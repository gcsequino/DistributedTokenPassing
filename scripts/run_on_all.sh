#! /bin/bash

curr_pane=0
function switch_to_pane {
    tmux select-pane -t "$1"
    curr_pane=$1
}

function set_up_windows {
    tmux split-window -v
    tmux select-pane -U
    tmux split-window -h
    tmux split-window -v
    tmux split-window -h
    tmux select-pane -U
    tmux split-window -h
    tmux select-pane -D
    tmux select-pane -D
    tmux split-window -h
    tmux split-window -v
    tmux split-window -h
    tmux select-pane -U
    tmux split-window -h
    tmux select-pane -R
    tmux split-window -v
    tmux split-window -h
    tmux select-layout tiled
}

function run_command {
    tmux send-keys -t "$curr_pane" "$1" Enter 
}

current_dir=$(pwd)
reg_ip=$(hostname)
reg_port=6969

set_up_windows

# switch_to_pane 12
switch_to_pane 11
run_command "cd $current_dir"
run_command "./build_and_run.sh -p 6969 -n 10 -r" 

machines_file="./scripts/machines.txt"
curl https://cs.colostate.edu/~dr101/good_machines.txt | head -n 10 > $machines_file
machines=$(cat $machines_file)
pane_num=1
sleep 7s
for machine in $machines; do
    echo "switching to pane $pane_num"
    switch_to_pane $pane_num
    
    run_command "echo sshing to $machine"
    run_command "ssh $machine"
    sleep 1s
    run_command "cd $current_dir"
    run_command "my_ip=\$(hostname)"
    run_command "java -cp ./app/build/libs/app.jar cs455.overlay.node.MessagingNode \${my_ip} ${reg_ip} ${reg_port}"
    ((pane_num += 1))
done

switch_to_pane 0

