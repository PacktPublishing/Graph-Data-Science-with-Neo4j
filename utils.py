import pandas as pd
import matplotlib.pyplot as plt


def create_projected_graph(gds, graph_name: str, **graph_config):
    """Helper function that will create a projected graph 
    with name `graph_name` and config as defined by the 
    `graph_config` kwargs. 

    **Drops a graph with same name if already exists.**
    """
    try:
        # try and get an existing graph object with same name
        graph_object = gds.graph.get(graph_name)
    except ValueError:
        graph_object = None
    # if graph already exists, drop it
    if graph_object:
        gds.graph.drop(graph_object)
    # create new projected graph
    graph_object, _ = gds.graph.project(graph_name, **graph_config)
    return graph_object


def plot_degree_distribution(degrees: pd.Series):
    # define figure and subplots
    fig = plt.figure(figsize=(12, 8))
    ax1, ax2 = fig.subplots(1, 2)

    # draw rank plot
    # sort degrees by values, and reset series index to plot
    # the node degrees occurrences
    sorted_degree = degrees.sort_values(ascending=False).reset_index().iloc[:,1]
    sorted_degree.plot(
        ax=ax1, 
        marker="o", color="k", 
        title="Degree Rank Plot", xlabel="Rank", ylabel="Degree",
        logy=True,
    )

    # compute and draw degree distribution
    distribution = degrees.value_counts().sort_index()
    distribution.plot(
        ax=ax2, 
        marker="o", color="k",
        title="Degree Distribution", xlabel="Degree", ylabel="nbOccurrences",
        logy=True, 
        # logx=True,
    )

    fig.tight_layout()
    return fig
