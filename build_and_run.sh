#! /bin/bash

# Args:
# Set the port: -p <port>
# Run a MessagingNode: -m
# Run a Registry: -r

# To run a MessagingNode pointing to a registry on ip 1.2.3.4:6969
# ./build_and_run.sh -i 1.2.3.4 -p 6969

# To run a registry:
# ./build_and_run.sh -r

./gradlew build

while getopts ":i:n:p:rm" o; do
    case "${o}" in
        i)
            registry_ip=${OPTARG}
            ;;
        p)
            registry_port=${OPTARG}
            ;;
        n)
            max_nodes=${OPTARG}
            ;;
        m)
            my_ip=$(curl ifconfig.me)
            echo "here"
            java -cp ./app/build/libs/app.jar cs455.overlay.node.MessagingNode "${my_ip}" "${registry_ip}" "${registry_port}"
            ;;
        r)
            java -cp ./app/build/libs/app.jar cs455.overlay.node.Registry "${registry_port}" "${max_nodes}"
            ;;
        *)
            echo "Read the comment at the top of build_and_run.sh"
            ;;
    esac
done