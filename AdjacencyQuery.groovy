def search
search = { graph, query, path = [] ->
    if (!query) return [path]

    def nodesToTest = path ? graph[path.last()] : graph.keySet()
    return nodesToTest.inject([] as Set) { results, node ->
        if (query.first()(path, node)) {
            results.addAll(search(graph, query.tail(), path + node))
        }
        return results
    }
}

def g = [
    alice: ['bob', 'carol'],
    bob: ['carol', 'alice'],
    carol: ['alice', 'bob']
]
def q = [
    { p, n -> n !in p },
    { p, n -> n !in p },
    { p, n -> n !in p && n != 'carol' },
]

search(g, q).each {
    println it.join(' => ')
}
