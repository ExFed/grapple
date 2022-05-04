import groovy.transform.*

@Canonical
class Traversal {
    def path = []
    def vars = [:]

    Traversal() {}

    Traversal(Traversal that) {
        this.@path = that.@path + []
        this.@vars = that.@vars + [:]
    }

    def path() { this.@path }

    def vars() { this.@vars }

    def last() { path.last() }

    boolean asBoolean() { path as boolean }

    Traversal leftShift(node) {
        path() << node
        return this
    }

    def getProperty(String id) { vars[id] }

    void setProperty(String id, def value) {
        if (vars.containsKey(id)) {
            throw new IllegalArgumentException("cannot rebind $id = ${vars[id]}")
        }
        vars[id] = value
    }

    String toString() {
        "Traversal($path, $vars)"
        // "Traversal($vars)"
    }
}

def search
search = { graph, query, traversal = new Traversal() ->
    if (!query) return [traversal]

    def nodes = traversal ? graph.adj[traversal.last()] : graph.adj.keySet()
    return nodes.inject([] as Set) { results, node ->
        def step = new Traversal(traversal) << node
        if (query.head()(step, graph.data[node])) {
            results.addAll(search(graph, query.tail(), step))
        }
        return results
    }
}

def printResults = { results, borders = true ->
    def horiz = borders ? '=' : ''
    def cross = borders ? '+' : ''
    def verti = borders ? '|' : ''

    def columns = results.inject([:].withDefault{ "$it".size() }) { acc, result ->
        def sizes = result.vars().collectEntries { k, v ->
            [k, Math.max("$v".size(), acc[k])]
        }
        acc << sizes
    }

    println columns.collect { name, size -> "$name".padRight(size) }.join(" $verti ")

    if (borders) {
        println columns.collect { name, size -> horiz * size }.join("$horiz$cross$horiz")
    }

    results.each { result ->
        println columns.collect { name, size -> "${result.vars()[name]}".padRight(size) }.join(" $verti ")
    }
}

def g = [
    data: [
        a: [name: 'Alice', birthday: 'June 3', favoriteFood: 'Pizza'],
        b: [name: 'Bob', birthday: 'Sept 29', favoriteFood: 'Applesauce'],
        c: [name: 'Carol', birthday: 'Feb 12', favoriteFood: 'Ice cream'],
        ab: 'likes',
        ac: 'knows',
        ba: 'knows',
        bc: 'likes',
        ca: 'likes',
        cb: 'knows',
    ],
    adj: [
        a: ['ab', 'ac'],
        b: ['bc', 'ba'],
        c: ['ca', 'cb'],
        ab: ['b'],
        ac: ['c'],
        ba: ['a'],
        bc: ['c'],
        ca: ['a'],
        cb: ['b'],
    ]
]

def q = [
    { t, d -> d instanceof Map && (t.'giver' = d.name) },
    { t, d -> d instanceof String && d == 'likes' },
    { t, d ->
        d instanceof Map
            && (t.'recipient' = d.name)
            && (t.'date' = d.birthday)
            && (t.'gift' = d.favoriteFood)
    },
]

def results = search(g, q)
println()
printResults(results)
println()
println "==> total results: ${results.size()}"
