
(*

This file contains the DSIsrc instrumentation frontend for C source files based on the C Intermediate Language (CIL)

change in ocamlutil/Makefile.ocaml:
COMPILE_FLAGS := $(COMPILEFLAGS) -warn-error -a

---------------------------------------------------------

Building cil with DSIsrc instrument extension:

apt-get install ocaml
apt-get install ocaml-findlib
symlink instrument.ml to cil-1.7.3/src/ext/instrument.ml
Add "Instrument.feature;" around line 90 in src/main.ml:
  let features : C.featureDescr list =
    ....
    Instrument.feature;

./configure
make
make test

*)

open Pretty
open Cil
open Str
open List
open Cfg
open Printf
module E = Errormsg
module H = Hashtbl

let trim s =
  let is_space = function
    | ' ' | '\012' | '\n' | '\r' | '\t' -> true
    | _ -> false in
  let len = String.length s in
  let i = ref 0 in
  while !i < len && is_space (String.get s !i) do
    incr i
  done;
  let j = ref (len - 1) in
  while !j >= !i && is_space (String.get s !j) do
    decr j
  done;
  if !i = 0 && !j = len - 1 then
    s
  else if !j >= !i then
    String.sub s !i (!j - !i + 1)
  else
    ""
;;

let excludeInstrumentationList = [
  "closeLog";
  "logComment";
  "checkLoggingIsSetup";
  "getAndIncEventId";
  "logOperationBegin";
  "logOperationEnd";
  "logBlockEntry";
  "logBlockExit";
  "logAssignNoOffset";
  "logAssignOffset";
  "logAssignArray";
  "logMallocInfo";
  "logReallocInfo";
  "logVarOutOfScope";
  "dumpVarsOutOfScope";
  "logFree";
  "dumpMap";
  "noInstrument_go";
  "printDllForward";
  "shadowDllInsert";
  "shadowDllDelete";
  "shadowDllIsMember";
  "shadowSllInsert";
  "shadowSllDelete";
  "shadowSllIsMember"]

let excludeFromInstrumentation name =
  if mem name excludeInstrumentationList then
    true
  else if (String.length name) < 6 then
    false
  else if (first_chars name 6) = "shadow" then
    true
  else
    false

let excludeDumpTypesList = [
    "exit";
    "malloc";
    "realloc";
    "free";
    "printf";
  ]

let excludeFromDumpTypes name =
  if excludeFromInstrumentation name then
    true
  else if mem name excludeDumpTypesList then
    true
  else
    false

let bool2int x =
  if x then
    1
  else
    0

(********************************************************************
Print xml
********************************************************************)


let rec ind x =
  if (x == 0) then "" else " " ^ (ind (x-1))

and xmlLhost (lh: lhost) q =
  (ind q) ^ "<lhost>\n" ^
  begin
    match lh with
      Var(vi) ->
        (ind (q+1)) ^ vi.vname ^ "\n"
    | Mem(e) ->
        xmlExp e (q+1)
  end
  ^ (ind q) ^ "</lhost>\n"

and xmlOffset (o: offset) q =
  (ind q) ^ "<offset>\n" ^
  match o with
    Field(f,newo) ->
      (ind (q+1)) ^ "<field fname=" ^ f.fname ^ ">\n"
      ^ xmlOffset newo (q+2)
      ^ (ind q) ^ "</field>\n"
    | _ -> ""
  ^ (ind q) ^ "</offset>\n"


and xmlLval (lv: lval) q =
  match lv with
    (lh,o) ->
      (ind q) ^ "<lval>\n"
      ^ (xmlLhost lh (q+1))
      ^ (xmlOffset o (q+1))
      ^ (ind q) ^ "</lval>\n"

and xmlExp (e: exp) q =
  (ind q) ^ "<exp>\n" ^
  match e with
    Lval(lv) -> (xmlLval lv (q+1))
  | _ -> ""
  ^ (ind q) ^ "</exp>\n"



let xmlInstr (i: instr) q =

  match i with
    Set(lv,e,_) ->
      (ind q) ^ "<assign>\n"
      ^ (ind (q+1)) ^ "<lhs>\n"
      ^ (xmlLval lv (q+2))
      ^ (ind (q+1)) ^ "</lhs>\n"
      ^ (ind (q+1)) ^ "<rhs>\n"
      ^ (xmlExp e (q+2))
      ^ (ind (q+1)) ^ "</rhs>\n"
      ^ (ind q) ^ "</assign>\n"
  | _ -> ""

(********************************************************************
Functions to assist logging
********************************************************************)

let logCommentFun =
    let fdec = emptyFunction "logComment" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("message", charConstPtrType, []);
                            ],
                            false, []);
    fdec

let logBlockEntryFun =
    let fdec = emptyFunction "logBlockEntry" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("filename", charConstPtrType, []);
                              ("linenumber", intType, []);
                            ],
                            false, []);
    fdec

let logBlockExitFun =
    let fdec = emptyFunction "logBlockExit" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("filename", charConstPtrType, []);
                              ("linenumber", intType, []);
                            ],
                            false, []);
    fdec

let logAssignNoOffsetFun =
    let fdec = emptyFunction "logAssignNoOffset" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("locFileName", charConstPtrType, []);
                              ("locLine", intType, []);

                              ("lvAddr", voidPtrType, []);
                              ("lvType", charConstPtrType, []);
                              ("lvStr", charConstPtrType, []);

                              ("lvContent", voidPtrType, []);
                              ("lvDerefType", charConstPtrType, []);
                              ("rhsStr", charConstPtrType, []);

                              ("isMalloc", intType, []);
                            ],
                            false, []);
    fdec

let logAssignOffsetFun =
    let fdec = emptyFunction "logAssignOffset" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("locFileName", charConstPtrType, []);
                              ("locLine", intType, []);

                              ("contextAddr", voidPtrType, []);
                              ("contextType", charConstPtrType, []);
                              ("contextStr", charConstPtrType, []);

                              ("lvAddr", voidPtrType, []);
                              ("lvType", charConstPtrType, []);
                              ("lvStr", charConstPtrType, []);

                              ("lvContent", voidPtrType, []);
                              ("lvDerefType", charConstPtrType, []);
                              ("rhsStr", charConstPtrType, []);

                              ("isMalloc", intType, []);
                            ],
                            false, []);
    fdec

let logAssignArrayFun =
    let fdec = emptyFunction "logAssignArray" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("eventPrefix", charConstPtrType, []);
                              ("eventSuffix", charConstPtrType, []);
                              ("locFileName", charConstPtrType, []);
                              ("locLine", intType, []);
                              ("aoType", charConstPtrType, []);
                              ("aoAddr", voidPtrType, []);
                              ("apType", charConstPtrType, []);
                              ("apAddr", voidPtrType, []);
                              ("targetAddr", voidPtrType, []);
                              ("rhsType", charConstPtrType, []);
                              ("lValCode", charConstPtrType, []);
                              ("isMalloc", intType, []);
                            ],
                            false, []);
    fdec

let logMallocInfoFun =
    let fdec = emptyFunction "logMallocInfo" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("mallocArg", charConstPtrType, []);
                              ("mallocArgVal", intType, []);
                            ],
                            false, []);
    fdec

let logReallocInfoFun =
    let fdec = emptyFunction "logReallocInfo" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("reallocPtrArg", charConstPtrType, []);
                              ("reallocPtrArgVal", voidPtrType, []);
                              ("reallocSizeArg", charConstPtrType, []);
                              ("reallocSizeArgVal", intType, []);
                            ],
                            false, []);
    fdec

let logVarOutOfScopeFun =
    let fdec = emptyFunction "logVarOutOfScope" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("varName", charConstPtrType, []);
                              ("varAddr", charConstPtrType, []);
                            ],
                            false, []);
    fdec

let dumpVarsOutOfScopeFun =
    let fdec = emptyFunction "dumpVarsOutOfScope" in
    fdec.svar.vtype <- TFun(voidType,
                            None,
                            false, []);
    fdec

let logFreeFun =
    let fdec = emptyFunction "logFree" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("locFileName", charConstPtrType, []);
                              ("locLine", intType, []);
                              ("freeArg", charConstPtrType, []);
                              ("freeArgVal", voidPtrType, []);
                            ],
                            false, []);
    fdec

let logVarInScope =
    let fdec = emptyFunction "logVarInScope" in
    fdec.svar.vtype <- TFun(voidType,
                            Some [
                              ("varKind", charConstPtrType, []);
                              ("varName", charConstPtrType, []);
                              ("varType", charConstPtrType, []);
                              ("addr", voidPtrType, []);
                            ],
                            false, []);
    fdec

(********************************************************************
Helpers
********************************************************************)

(* Returns true if the given lvalue offset ends in a bitfield access. *)
let rec is_bitfield lo = match lo with
  | NoOffset -> false
  | Field(fi,NoOffset) -> not (fi.fbitfield = None)
  | Field(_,lo) -> is_bitfield lo
  | Index(_,lo) -> is_bitfield lo

(* Return an expression that evaluates to the address of the given lvalue.
 * For most lvalues, this is merely AddrOf(lv). However, for bitfields
 * we do some offset gymnastics.
 *)
let addr_of_lv (lh,lo) =
  if is_bitfield lo then begin
    (* we figure out what the address would be without the final bitfield
     * access, and then we add in the offset of the bitfield from the
     * beginning of its enclosing comp *)
    let rec split_offset_and_bitfield lo = match lo with
      | NoOffset -> failwith "logwrites: impossible"
      | Field(fi,NoOffset) -> (NoOffset,fi)
      | Field(e,lo) ->  let a,b = split_offset_and_bitfield lo in
                        ((Field(e,a)),b)
      | Index(e,lo) ->  let a,b = split_offset_and_bitfield lo in
                        ((Index(e,a)),b)
    in
    let new_lv_offset, bf = split_offset_and_bitfield lo in
    let new_lv = (lh, new_lv_offset) in
    let enclosing_type = TComp(bf.fcomp, []) in
    let bits_offset, bits_width =
      bitsOffset enclosing_type (Field(bf,NoOffset)) in
    let bytes_offset = bits_offset / 8 in
    let lvPtr = mkCast ~e:(mkAddrOf (new_lv)) ~newt:(charPtrType) in
    (BinOp(PlusPI, lvPtr, (integer bytes_offset), ulongType))
  end else (AddrOf (lh,lo))

let typeToString t =
  trim (Pretty.sprint 80 (Pretty.dprintf "%a" d_type t))

let lvalToString lv =
  trim (Pretty.sprint 80 (Pretty.dprintf "%a" d_lval lv))

let expToString e =
  trim(Pretty.sprint 80 (Pretty.dprintf "%a" d_exp e))

let stmtToString s =
  trim(Pretty.sprint 80 (Pretty.dprintf "%a" d_stmt s))

let lvalToString lv =
  trim(Pretty.sprint 80 (Pretty.dprintf "%a" d_lval lv))

let typeIsPointer (t: typ) =
  match unrollTypeDeep t with
    TPtr(_,_) -> true
  | _ -> false

let isStructWithPointerFields (t:typ) =
  let rec hasPointerFields (fields:fieldinfo list) =
    match fields with
      (f::fs) ->
        begin
        match (unrollTypeDeep f.ftype) with
            TPtr(_,_) -> true
          | TComp(comp,_) -> (hasPointerFields comp.cfields) || (hasPointerFields fs)
          | _ -> false
        end
    | [] -> false
  in
  match (unrollTypeDeep t) with
    TComp(comp,_) ->
      hasPointerFields comp.cfields
  | _ -> false

let rec unrollAllPtrs (t:typ) =
  match t with
    TPtr(t2,_) -> unrollAllPtrs t2
  | _ -> t


let findFieldNameIndex fieldList fname =
  let rec findFieldNameIndexHelper fieldList fname count =
    match fieldList with
      (field::fields) -> if (field.fname = fname) then count
                         else findFieldNameIndexHelper fields fname (count+1)
      | [] -> begin ignore (Errormsg.log "Couldn't find fname in fieldList\n"); 0 end
  in
  findFieldNameIndexHelper fieldList fname 0

let rec howManyOffsets (off: offset) i =
  match off with
    (NoOffset) -> i
  | Field(fi,off2) -> howManyOffsets off2 (i+1)



(********************************************************************
Instrumentation for assignment and malloc
********************************************************************)

let instrumentationForAssignNoOffset loc lv lvType lvDerefType rhs isMalloc =
  let lvAddr = addr_of_lv lv in
  let lvContent = Lval(lv) in
  begin
    [
      Call(
        None,
        (Lval(Var(logAssignNoOffsetFun.svar),NoOffset)),
        [
          mkString loc.file;
          integer loc.line;

          lvAddr;
          mkString (typeToString lvType);
          mkString (lvalToString lv);

          lvContent;
          mkString (typeToString lvDerefType);
          mkString (expToString rhs);

          integer (bool2int isMalloc);
        ],
        loc)
    ]
  end

let instrumentationForAssignOffset loc lv lvType lvDerefType rhs isMalloc contextAddr contextType contextStr =
  let lvAddr = addr_of_lv lv in
  let lvContent = Lval(lv) in
  begin
    [
      Call(
        None,
        (Lval(Var(logAssignOffsetFun.svar),NoOffset)),
        [
          mkString loc.file;
          integer loc.line;

          contextAddr;
          mkString (typeToString contextType);
          mkString contextStr;

          lvAddr;
          mkString (typeToString lvType);
          mkString (lvalToString lv);

          lvContent;
          mkString (typeToString lvDerefType);
          mkString (expToString rhs);

          integer (bool2int isMalloc);
        ],
        loc)
    ]
  end

let instrumentationForMallocCall (es: exp list) (loc) =
  match es with
  | (eSize::[]) ->
    Call(
      None,
      (Lval(Var(logMallocInfoFun.svar),NoOffset)),
      [
        mkString (expToString eSize);
        eSize;
      ],
      loc)
  | _ -> failwith "Malloc call with incorrect number of args\n"

let instrumentationForReallocCall (es: exp list) (loc) =
  match es with
  | (ePtr::(eSize::[])) ->
    Call(
      None,
      (Lval(Var(logReallocInfoFun.svar),NoOffset)),
      [
        mkString (expToString ePtr);
        ePtr;
        mkString (expToString eSize);
        eSize;
      ],
      loc)
  | _ -> failwith "Realloc call with incorrect number of args\n"

let isSelfRefPointer (baseType:typ) (ptrToBaseType:typ) =
    (* ignore (Errormsg.log "\t\t\t\t isSelfRefPointer %s | %s\n" (typeToString baseType) (typeToString ptrToBaseType)); *)
  begin
    match (baseType,(unrollTypeDeep ptrToBaseType)) with
      (TComp(baseComp,_),TPtr(TComp(childComp,_),_)) when baseComp == childComp -> true
    | _ -> false
  end


let rec getSelfRefFields (compType:typ) (fields:fieldinfo list) =
  match fields with
    (f::fs) ->
      if isSelfRefPointer compType f.ftype then
        [f.fname] @ getSelfRefFields compType fs
      else
        getSelfRefFields compType fs
  | [] -> []

let isStructWithSelfRefFields (t:typ) =
  (* ignore (Errormsg.log "\t\t\t isStructWithSelfRefFields %s\n" (typeToString t)); *)
  match t with
    TComp(comp,_) ->
      (getSelfRefFields t comp.cfields) != []
  | _ -> false

let genLvLogCall lv rhs loc isMalloc =
  (* Assumes lv will be of type TPtr(_,_) *)

  let lvType = unrollTypeDeep (typeOfLval lv) in
    match lvType with
      TPtr(lvDerefType,_) ->

        match lv with
          (_, NoOffset) -> (* Includes (Mem(e),NoOffset) and (Var(vi),NoOffset) *)
            instrumentationForAssignNoOffset loc lv lvType lvDerefType rhs isMalloc

        | (Mem(e),_) ->
            let contextType =
              match unrollTypeDeep (typeOf e) with
                TPtr(t,_) -> (unrollTypeDeep t)
              | _ -> unrollTypeDeep (typeOf e)
            and contextAddr = e (* addr_of_lv (Mem(e),NoOffset) = e (deref then take addr, view .cil.c for evidence) *)
            and contextStr = lvalToString (Mem(e),NoOffset)
            in
            instrumentationForAssignOffset loc lv lvType lvDerefType rhs isMalloc contextAddr contextType contextStr

        | (Var(vi), _) ->
            let contextType =
              match unrollTypeDeep vi.vtype with
                TPtr(t,_) -> (unrollTypeDeep t)
              | _ -> unrollTypeDeep vi.vtype
            and contextAddr = addr_of_lv (Var(vi),NoOffset)
            and contextStr = lvalToString (Var(vi),NoOffset)
            in
            instrumentationForAssignOffset loc lv lvType lvDerefType rhs isMalloc contextAddr contextType contextStr

  | _ -> failwith "FAILURE: lvType was not of type TPtr(_,_)\n"

class logWriteVisitor = object
  inherit nopCilVisitor

  method vinst (i: instr) : instr list visitAction =
    match i with

      Set(lv, rhs, loc)
          when (typeIsPointer (typeOfLval lv)) ->
        let logInstr = genLvLogCall lv rhs loc false
        in
        ChangeTo ([i] @ logInstr)

    | Call(Some lv,((Lval(Var(callVarInfo),NoOffset)) as rhs),args,loc)
          when (typeIsPointer (typeOfLval lv)) ->
        let extraMallocLog =
          match callVarInfo.vname with
            "malloc" -> [instrumentationForMallocCall args loc]
          | "realloc" -> [instrumentationForReallocCall args loc]
          | _        -> []
        in
        let isMalloc = extraMallocLog != []
        in
        let logInstr = genLvLogCall lv rhs loc isMalloc
        in
        ChangeTo ([i] @ logInstr @ extraMallocLog)

    | Call(Some lv, rhs, args, loc)
          when (typeIsPointer (typeOfLval lv)) ->
        let logInstr = genLvLogCall lv rhs loc false
        in
        ChangeTo ([i] @ logInstr)

    | _ -> SkipChildren

end


class generatePointerWriteLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      SkipChildren
    else
      let vis = new logWriteVisitor in
      begin
        visitCilFunction vis f;
        SkipChildren
      end
end

(********************************************************************
Instrumentation for vars going out of scope
********************************************************************)

let rec instrumentationForVarOutOfScope (clearVarList: varinfo list) =
  match clearVarList with
    (v::vs) ->
      begin
        let call = Call((None), (Lval(Var(logVarOutOfScopeFun.svar),NoOffset)),
                         [
                           mkString v.vname;
                           addr_of_lv (Var(v),NoOffset);
                         ], locUnknown)
        in
        if typeIsPointer v.vtype || isStructWithPointerFields v.vtype then
          [call] @ instrumentationForVarOutOfScope vs
        else
          instrumentationForVarOutOfScope vs
      end
  | [] -> []

class addClearCallsToReturns (clearCalls: instr list)  = object (self)
  inherit nopCilVisitor

  method vstmt (s: stmt) =
    match s.skind with
      Return(exp,loc) ->
        let newReturnStmt = mkStmt s.skind
        and clearInstList = mkStmt (Instr(clearCalls))
        in
        let blockStmt = mkStmt (Block(mkBlock [clearInstList; newReturnStmt]))
        in
          begin
            blockStmt.labels <- s.labels;
            (*blockStmt.sid = s.sid, setting this and succs/preds is unnecessary since recomputed after cfg call *)
            ChangeTo blockStmt
          end
    | _ -> DoChildren

end

class generateVarsOutOfScopeLogging = object (self)
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      SkipChildren
    else
      let clearCalls = instrumentationForVarOutOfScope (f.sformals @ f.slocals)
      in
      let clearReturnVisitor = new addClearCallsToReturns clearCalls
      in
      begin
        visitCilFunction clearReturnVisitor f;
        SkipChildren
      end

end


let dumpVarsOutOfScopeCall =
  Call(
    None,
     (Lval(Var(dumpVarsOutOfScopeFun.svar),NoOffset)),
        [],
        locUnknown)

class generateDumpVarsOutOfScope = object (self)
  inherit nopCilVisitor

  method vinst (i: instr) : instr list visitAction =
    match i with
      Call(maybe_lv, ((Lval(Var(callVarInfo),NoOffset)) as rhs), args, loc)
          when not (excludeFromDumpTypes callVarInfo.vname) ->
        ChangeTo ([i] @ [dumpVarsOutOfScopeCall])

    | _ -> SkipChildren

end

(********************************************************************
Instrumentation for free
********************************************************************)

let instrumentationForFree (e: exp) loc =
  Call((None), (Lval(Var(logFreeFun.svar),NoOffset)),
       [
         mkString loc.file;
         integer loc.line;
         mkString (expToString e);
         e
       ], locUnknown)


class instrumentFreeCalls = object (self)
  inherit nopCilVisitor

  method vinst (i: instr) =
    match i with
      Call(maybe_lv,(Lval(Var(callVarInfo),NoOffset)),(arg1::[]),loc)
          when (   (compare callVarInfo.vname "free" == 0)
                || (compare callVarInfo.vname "zfree" == 0)) ->
        ChangeTo
        [
          (instrumentationForFree arg1 loc);
          i
        ]
    | _ -> SkipChildren

end

class generateFreeLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      SkipChildren
    else
      let vis = new instrumentFreeCalls in
      begin
        visitCilFunction vis f;
        SkipChildren
      end
end
(********************************************************************
Remove registers
********************************************************************)

class removeRegsVisitor = object
  inherit nopCilVisitor

  method vvdec (vdec: varinfo) =
    match vdec.vstorage with

      Register ->
        begin
          vdec.vstorage <- NoStorage;
          SkipChildren
        end
    | _ -> SkipChildren

end

let continueBlocksOverTheseFunctions = [
  "logAssignNoOffset";
  "logAssignOffset";
  "logAssignArray";
  "logMallocInfo";
  "logReallocInfo";
  "logFree";
  "logComment";
  "logVarOutOfScope";
  "dumpVarsOutOfScope";
  "malloc";
  "realloc";
  "kzalloc";
  "vmalloc";
  "zmalloc";
  "calloc";
  "zcalloc";
  "free"]

let rec containsAtLeastOneLogCall (instList: instr list) =
  match instList with
    (i::is) ->
      begin
        match i with
          Call(_,(Lval(Var(callVarInfo),NoOffset)),_,_) as call
                when mem callVarInfo.vname continueBlocksOverTheseFunctions ->
              true
        | _ -> (containsAtLeastOneLogCall is)
      end
  | [] -> false

let rec doInst (instrList: instr list) (inBlock: bool) (lastInst: instr) =
  let entryCall location =
   Call(
      None,
      (Lval(Var(logBlockEntryFun.svar),NoOffset)),
      [
        mkString location.file;
        integer location.line;
      ],
      locUnknown)
  and exitCall location =
    Call(
      None,
      (Lval(Var(logBlockExitFun.svar),NoOffset)),
      [
        mkString location.file;
        integer location.line;
      ],
      locUnknown)
  in
  match instrList with
    (i::is) ->
      begin
        (* ignore (Errormsg.log "%s\n" (xmlInstr i 0)); *)
        begin
          match i with
            Asm(_,_,_,_,_,_) as other ->
              if not inBlock then
                [entryCall (get_instrLoc i); other] @ (doInst is true i)
              else
                [other] @ (doInst is true i)

          | Set(_,_,_) as other ->
              if not inBlock then
                [entryCall (get_instrLoc i); other] @ (doInst is true i)
              else
                [other] @ (doInst is true i)

          | Call(_,(Lval(Var(callVarInfo),NoOffset)),_,_) as call
                when mem callVarInfo.vname continueBlocksOverTheseFunctions ->
              if not inBlock then
                [entryCall (get_instrLoc i); call] @ (doInst is true i)
              else
                [call] @ (doInst is true i)

          | Call(_,_,_,_) as call ->
              if inBlock then
                ([exitCall (get_instrLoc i); call]
                  @ (doInst is false i))
              else
                ([call]
                  @ (doInst is false i))

        end
      end
  | [] ->
      if inBlock then
        [exitCall (get_instrLoc lastInst)]
      else
        []

class instrumentBasicBlocks = object
  inherit nopCilVisitor

  method vstmt (s: stmt) =
    match s.skind with
      Instr(instList) when (length instList) > 0 && (containsAtLeastOneLogCall instList) ->
        let newStmt = mkStmt (Instr(doInst instList false (hd instList)))
        in
          begin
            newStmt.labels <- s.labels;
            ChangeTo newStmt
          end
    | _ -> DoChildren
end


class generateBasicBlockLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      begin
        ignore (Errormsg.log "\nSkipped %s\n" f.svar.vname);
        SkipChildren
      end
    else
      let vis = new instrumentBasicBlocks in
      begin
        ignore (Errormsg.log "\nGened %s\n" f.svar.vname);
        visitCilFunction vis f;
        SkipChildren
      end

end

(********************************************************************
Mark beginnings and ends of functions
********************************************************************)

let rec genCallsForVarsInScope (vars:varinfo list) (varType:string) =
  let genCallForVarInScope (var:varinfo) (varType:string) =
    Call((None), (Lval(Var(logVarInScope.svar),NoOffset)),
                       [
                          mkString varType;
                          mkString var.vname;
                          mkString (typeToString var.vtype);
                          addr_of_lv (Var(var),NoOffset);

                       ], locUnknown)
  in
  match vars with
    (v::vs) ->
      if typeIsPointer v.vtype || isStructWithPointerFields v.vtype then
        [genCallForVarInScope v varType] @ (genCallsForVarsInScope vs varType)
      else
        (genCallsForVarsInScope vs varType)
  | [] -> []

class generateFunctionEnterLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      begin
        ignore (Errormsg.log "\nSkipped marking %s\n" f.svar.vname);
        SkipChildren
      end
    else
      let funEnteredCall = Call((None), (Lval(Var(logCommentFun.svar),NoOffset)),
                       [
                         mkString "Function Entered";
                       ], locUnknown)
      in
      let formalVarsInScopeCalls = genCallsForVarsInScope f.sformals "formal"
      and localVarsInScopeCalls = genCallsForVarsInScope f.slocals "local"
      in
      let loggingStmt = mkStmt (Instr([funEnteredCall] @ formalVarsInScopeCalls @ localVarsInScopeCalls))
      in
      match f.sbody.bstmts with
        (oldStmtsHead::oldStmtsTail) ->
          begin
            loggingStmt.labels <- oldStmtsHead.labels;
            oldStmtsHead.labels <- [];
            f.sbody.bstmts <- [loggingStmt] @ [oldStmtsHead] @ oldStmtsTail;
            SkipChildren
          end
      | _ -> ignore (); SkipChildren (* big error if we get here, function has empty list of stmts - i.e. no return! *)
end

class generateFunctionExitLoggingStmt = object (self)
  inherit nopCilVisitor

  method vstmt (s: stmt) =
    match s.skind with
      Return(exp,loc) ->
        let newReturnStmt = mkStmt s.skind
        and call = Call((None), (Lval(Var(logCommentFun.svar),NoOffset)),
                         [
                           mkString "Function Exited";
                         ], locUnknown)
        in
        let loggingStmt = mkStmt (Instr([call]))
        in
        let blockStmt = mkStmt (Block(mkBlock [loggingStmt; newReturnStmt]))
        in
          begin
            blockStmt.labels <- s.labels;
            (*blockStmt.sid = s.sid, setting this and succs/preds is unnecessary since recomputed after cfg call *)
            ChangeTo blockStmt
          end
    | _ -> DoChildren

end

class generateFunctionExitLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      SkipChildren
    else
      let vis = new generateFunctionExitLoggingStmt in
      begin
        visitCilFunction vis f;
        SkipChildren;
      end

end


(********************************************************************
Dump types
********************************************************************)

let rec printFields (fh:out_channel) (comp:typ) (fields:fieldinfo list) (index) =
  match fields with
    (f::fs) ->
      let (offset,width) = bitsOffset comp (Field(f,NoOffset)) in
      begin
        fprintf fh "\t\t\t<field index=\"%d\">\n" index;
        fprintf fh "\t\t\t\t<name>%s</name>\n" f.fname;
        fprintf fh "\t\t\t\t<sugaredType>%s</sugaredType>\n" (typeToString f.ftype);
        fprintf fh "\t\t\t\t<desugaredType>%s</desugaredType>\n" (typeToString (unrollType f.ftype));
        fprintf fh "\t\t\t\t<sizeInBits>%d</sizeInBits>\n" (bitsSizeOf f.ftype);
        fprintf fh "\t\t\t\t<offsetInBytes>%d</offsetInBytes>\n" (offset/8);
        fprintf fh "\t\t\t\t<location file=\"%s\" line=\"%d\" col=\"0\"/>\n" f.floc.file f.floc.line;
        fprintf fh "\t\t\t</field>\n";
        printFields fh comp fs (index+1);
      end
  | [] -> ignore ()

class dumpTypes (fh:out_channel) = object (self)
  inherit nopCilVisitor

  method vglob(g:global) =
    match g with
      GType(typeinfo,loc) ->
        begin
          fprintf fh "\t<alias>\n";
          fprintf fh "\t\t<sugaredType>%s</sugaredType>\n" typeinfo.tname;
          fprintf fh "\t\t<desugaredType>%s</desugaredType>\n" (typeToString (unrollType typeinfo.ttype));
          fprintf fh "\t\t\t\t<sizeInBits>%d</sizeInBits>\n" (bitsSizeOf typeinfo.ttype);
          fprintf fh "\t\t<location file=\"%s\" line=\"%d\" col=\"0\"/>\n" loc.file loc.line;
          fprintf fh "\t</alias>\n";
          DoChildren;
        end
    | GCompTag(compinfo,loc) ->
        begin

          if (compinfo.cstruct) then
            fprintf fh "\t<compound mode=\"struct\">\n"
          else
            fprintf fh "\t<compound mode=\"union\">\n";
          fprintf fh "\t\t<tagName>%s</tagName>\n" compinfo.cname;
          fprintf fh "\t\t<sizeInBytes>%d</sizeInBytes>\n" ((bitsSizeOf (TComp(compinfo,[])))/8);
          fprintf fh "\t\t<location file=\"%s\" line=\"%d\" col=\"0\"/>\n" loc.file loc.line;
          fprintf fh "\t\t<fields>\n";
          printFields fh (TComp(compinfo,[])) compinfo.cfields 0;
          fprintf fh "\t\t</fields>\n";
          fprintf fh "\t</compound>\n";
          DoChildren;
        end
    | _ -> DoChildren

end

let printTypes (f:file) =
  let typeFileName = (String.concat "_" [f.fileName; "types.xml"]) in
  let typeFile = open_out typeFileName in
  let logTypes = new dumpTypes typeFile in
  begin
    ignore (Errormsg.log "\nDumping types %s\n" typeFileName);
    fprintf typeFile "<types arch=\"%d\">\n" (bitsSizeOf charPtrType);
    visitCilFileSameGlobals logTypes f;
    fprintf typeFile "</types>\n";
    close_out typeFile;
  end

(********************************************************************
Instrument loops
********************************************************************)

class generateBreakContinueLogging = object (self)
  inherit nopCilVisitor

  method vstmt (s: stmt) =
    begin
      let call = Call((None), (Lval(Var(logCommentFun.svar),NoOffset)),
                       [
                         mkString "Loop-iteration-end";
                       ], locUnknown)
      in
      match s.skind with
      | Switch (_,_,_,_) -> SkipChildren

      | Break(loc) ->
          let newStmt = mkStmt (Block (mkBlock([(mkStmt (Instr([call])))] @ [(mkStmt (Break(loc)))]))) in
          newStmt.labels <- s.labels;
          ChangeTo newStmt


      | Continue(loc) ->
          let newStmt = mkStmt (Block (mkBlock([(mkStmt (Instr([call])))] @ [(mkStmt (Continue(loc)))]))) in
          newStmt.labels <- s.labels;
          ChangeTo newStmt

      | _ -> DoChildren
    end

end


class generateLoopLoggingOfStmt = object (self)
  inherit nopCilVisitor

(* Does not work with labels that point directly to first stmt in loop body,
   see commented code below. Cil docs says we need to update ref in goto, but
   it's not necessary for othercases, e.g. in generateBreakContinueLogging *)

  method vstmt (s: stmt) =
    begin
      match s.skind with
        Loop(blk,loc,a,b) ->
          let enterCall = Call((None), (Lval(Var(logCommentFun.svar),NoOffset)),
                           [
                             mkString (String.concat "_" ["Loop-iteration-start";loc.file;string_of_int loc.line]);
                           ], locUnknown)
          in
          let enterLoggingStmt = mkStmt (Instr([enterCall])) in
          let exitCall = Call((None), (Lval(Var(logCommentFun.svar),NoOffset)),
                           [
                             mkString "Loop-iteration-end";
                           ], locUnknown)
          in
          let exitLoggingStmt = mkStmt (Instr([exitCall])) in
          let vis = new generateBreakContinueLogging in
          let block1 = visitCilStmt vis (mkStmt (Block(mkBlock (tl blk.bstmts)))) in
          let block2 = mkBlock ([hd blk.bstmts] @ [enterLoggingStmt] @ [block1] @ [exitLoggingStmt]) in
          begin
            let onPost =
                mkStmt (Loop(block2,loc,a,b))
            in
            ChangeDoChildrenPost(s,(fun i -> onPost))
          end

      | _ -> DoChildren
    end

end

class generateLoopLogging = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    if (excludeFromInstrumentation f.svar.vname) then
      SkipChildren
    else
      let vis = new generateLoopLoggingOfStmt in
      begin
        ignore (Errormsg.log "Visiting function %s\n" f.svar.vname);
        visitCilFunction vis f;
        SkipChildren;
      end

end


(********************************************************************
Main
********************************************************************)

class printCfgVisitor = object
  inherit nopCilVisitor

  method vfunc(f:fundec) =
    begin
      printCfgFilename f.svar.vname f;
      SkipChildren
    end

end


let feature : featureDescr =
  { fd_name = "instrument";
    fd_enabled = ref false;
    fd_description = "generation of code to log memory writes and types";
    fd_extraopt = [];
    fd_doit =
    (function (f: file) ->

      let rrVisitor = new removeRegsVisitor
      and logPointerWrites = new generatePointerWriteLogging
      and logVarsOutOfScope = new generateVarsOutOfScopeLogging
      and logFrees = new generateFreeLogging
      and logBasicBlocks = new generateBasicBlockLogging
      and printCfg = new printCfgVisitor
      and logFunctionEnter = new generateFunctionEnterLogging
      and logFunctionExit = new generateFunctionExitLogging
      and loopEnterExit = new generateLoopLogging
      and dumpVarsOutOfScope = new generateDumpVarsOutOfScope

      in
      begin
        visitCilFile rrVisitor f;
        visitCilFileSameGlobals dumpVarsOutOfScope f;
        visitCilFileSameGlobals logPointerWrites f;
        visitCilFileSameGlobals logFrees f;
        visitCilFileSameGlobals logBasicBlocks f;
        visitCilFileSameGlobals logVarsOutOfScope f;
        visitCilFileSameGlobals logFunctionEnter f;
        visitCilFileSameGlobals logFunctionExit f;
        printTypes f;
        visitCilFileSameGlobals loopEnterExit f;
      end
    );
    fd_post_check = true;
  }


