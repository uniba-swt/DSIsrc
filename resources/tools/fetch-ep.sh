grep aggCnt * | sed -e 's/^.*ep: //' -e 's/ start.*aggCnt: /,/' -e 's/<.*//' | sort -t, -nk2 | tail -n 1 | sed -e s'/\([0-9]\+\).*/x_\1.dot/' | xargs lg | xargs xdot&
