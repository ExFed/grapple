package com.columnzero.grapple

import groovy.transform.*
import java.net.URLEncoder

@CompileStatic
class Graph {

    private static final URI GRAPH_URI = URI.create('grapple://graph/')
    private static final Closure<Set<Edge>> SET_CTOR = { [] as Set<Edge> }

    private final Map<URI, Object> data = [:] // uri -> data
    private final Map<URI, Node> nodes = [:].withDefault{ k -> new Node(asUri(k))} // uri -> node

    private final Set<Edge> edges = [] as Set
    private final Map<Object, Set<Edge>> srIndex = [:].withDefault(SET_CTOR) // (s, r) -> [edge]
    private final Map<Object, Set<Edge>> rtIndex = [:].withDefault(SET_CTOR) // (r, t) -> [edge]
    private final Map<Object, Set<Edge>> stIndex = [:].withDefault(SET_CTOR) // (s, t) -> [edge]
    private final Map<URI, Set<Edge>> sIndex = [:].withDefault(SET_CTOR) // s -> [edge]
    private final Map<URI, Set<Edge>> rIndex = [:].withDefault(SET_CTOR) // r -> [edge]
    private final Map<URI, Set<Edge>> tIndex = [:].withDefault(SET_CTOR) // t -> [edge]

    URI asUri(Object key) {
        def keyUri = switch (key.getClass()) {
            case URI: yield (URI) key
            case URL: yield ((URL) key).toURI()
            case Node: yield ((Node) key).uri
            case Edge: yield ((Edge) key).relation
            case String: yield URI.create(URLEncoder.encode((String) key) + '/')
            default: throw new IllegalArgumentException("unsupported key type: ${key.getClass()}")
        }
        this.uri.resolve(keyUri)
    }

    URI getUri() { GRAPH_URI }

    Node node(def key) {
        def uri = asUri(key)
        uri in nodes ? nodes[uri] : node(uri, null)
    }

    Node node(def key, def data) {
        def n = new Node(asUri(key))
        n.data = data
        nodes[n.uri] = n
    }

    Edge edge(Object s, Object r, Object t) {
        def e = new Edge(asUri(s), asUri(r), asUri(t))
        edge(e)
    }

    Edge edge(Edge e) {
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

    Node leftShift(def key) { node(key) }

    Set<Edge> getRelationships(def relation) { rIndex[asUri(relation)].asUnmodifiable() }

    @Immutable
    @ToString(includes = ['uri'])
    class Node {
        URI uri

        Object getData() { Graph.this.data[uri] }
        Object setData(Object value) { Graph.this.data[uri] = value }

        EdgeBuilder rightShift(def r) { new EdgeBuilder(uri, r) }
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

        Object getData() { Graph.this.data[r] }
        Object setData(def value) { Graph.this.data[r] = value }
    }

    @Canonical
    class EdgeBuilder {
        final Object source
        final Object relation

        Edge rightShift(Object target) { edge(source, relation, target) }
    }
}
