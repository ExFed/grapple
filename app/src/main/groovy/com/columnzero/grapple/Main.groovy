package com.columnzero.grapple

class Main {
    static void main(String[] args) {
        println "Hello, world!"
        args.eachWithIndex { arg, i -> println "args[$i] = $arg" }
    }
}
