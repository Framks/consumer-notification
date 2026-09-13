FROM ubuntu:latest
LABEL authors="galve"

ENTRYPOINT ["top", "-b"]