package com.columnzero.grapple

import groovy.transform.*

class Graph {

    private static final def GRAPH_URI = URI.create('grapple://graph/')
    private static final def SET_CTOR = { [] as Set }

    private final Map<URI, Object> data = [:] // uri -> data
    private final Map<URI, Node> nodes = [:].withDefault{ k -> new Node(asUri(k))} // uri -> node

    private final Set<Edge> edges = [] as Set
    private final Map<Object, Set<Edge>> srIndex = [:].withDefault(SET_CTOR) // (s, r) -> [edge]
    private final Map<Object, Set<Edge>> rtIndex = [:].withDefault(SET_CTOR) // (r, t) -> [edge]
    private final Map<Object, Set<Edge>> stIndex = [:].withDefault(SET_CTOR) // (s, t) -> [edge]
    private final Map<URI, Set<Edge>> sIndex = [:].withDefault(SET_CTOR) // s -> [edge]
    private final Map<URI, Set<Edge>> rIndex = [:].withDefault(SET_CTOR) // r -> [edge]
    private final Map<URI, Set<Edge>> tIndex = [:].withDefault(SET_CTOR) // t -> [edge]

    def asUri(def key) {
        def keyUri = switch (key.getClass()) {
            case URI: yield key
            case URL: yield key.toURI()
            case Node: yield key.uri
            case Edge: yield key.relation
            case String: yield "${java.net.URLEncoder.encode(key)}/"
            default: throw new IllegalArgumentException("unsupported key type: ${key.getClass()}")
        }
        this.uri.resolve(keyUri)
    }

    def getUri() { GRAPH_URI }

    def node(def key) {
        def uri = asUri(key)
        uri in nodes ? nodes[uri] : node(uri, null)
    }

    def node(def key, def data) {
        def n = new Node(asUri(key))
        n.data = data
        nodes[n.uri] = n
    }

    def edge(def s, def r, def t) {
        def e = new Edge(asUri(s), asUri(r), asUri(t))
        edge(e)
    }

    def edge(Edge e) {
        if (e !in edges) {
            edges << e
            srIndex[[e.s, e.r]] << e
            rtIndex[[e.r, e.t]] << e
            stIndex[[e.s, e.t]] << e
            sIndex[e.s] << e
            rIndex[e.r] << e
            tIndex[e.t] << e
        }
        return e
    }

    def leftShift(def key) { node(key) }

    def getRelationships(def relation) { rIndex[asUri(relation)] }

    @Immutable
    @ToString(includes = ['uri'])
    class Node {
        URI uri

        def getData() { Graph.this.data[uri] }
        def setData(def value) { Graph.this.data[uri] = value }

        def rightShift(def r) { new EdgeBuilder(uri, r) }
    }

    @Immutable
    @ToString(includes = ['source', 'relation', 'target'])
    class Edge {
        URI source
        URI relation
        URI target

        URI getS() { source }
        URI getR() { relation }
        URI getT() { target }

        Node getSourceNode() { nodes[s] }
        Node getTargetNode() { nodes[t] }

        def getData() { Graph.this.data[r] }
        def setData(def value) { Graph.this.data[r] = value }
    }

    @Canonical
    class EdgeBuilder {
        final def source
        final def relation

        def rightShift(def target) { edge(source, relation, target) }
    }
}
