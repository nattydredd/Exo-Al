function renderTimeseries(dataFilePath, targetElement) {

    //Set the dimensions of the light curve box
    var width = 1200,
            height = 600;

    //Set the scale ranges
    var x = d3.scaleLinear().range([0, width]);
    var y = d3.scaleLinear().range([height, 0]);

    //Get the data
    d3.csv(dataFilePath, function (error, data) {
        if (error)
            throw error;

        //Format the data
        data.forEach(function (d) {
            d.time = +d.time;
            d.lum = +d.lum;
        });

        //Scale the range of the data
        x.domain(d3.extent(data, function (d) {
            return d.time;
        }));
        y.domain(d3.extent(data, function (d) {
            return d.lum;
        }));

        //D3 Zoom
        var zoom = d3.zoom()
                .scaleExtent([1, 5])
                .translateExtent([
                    [-100, -100],
                    [width + 90, height + 100]
                ])
                .on("zoom", zoomed);

        //Timeseries canvas
        var svg = targetElement.append("svg")
                .classed("canvas", true)
//                .attr("id", "canvas")
                .attr("width", width)
                .attr("height", height);

        //Create and append axis
        var xAxis = d3.axisBottom(x)
                .ticks((width + 2) / (height + 2) * 10)
                .tickSize(height)
                .tickPadding((height - 12) - height);

        var yAxis = d3.axisRight(y)
                .ticks(10)
                .tickSize(width)
                .tickPadding(4 - width);

        var gX = svg.append("g")
                .attr("class", "axis xAxis")
                .call(xAxis);

        var gY = svg.append("g")
                .attr("class", "axis yAxis")
                .call(yAxis);

        //Rectangle to hold svg for panning and zooming
        var view = svg.append("rect")
                .attr("class", "view")
                .attr("x", 0.5)
                .attr("y", 0.5)
                .attr("width", width - 1)
                .attr("height", height - 1);

        //Axis labels
        svg.append("text")
                .attr("class", "axisLabel")
                .attr("transform",
                        "translate(" + (width / 2) + " ," +
                        (height - 20) + ")")
                .style("text-anchor", "middle")
                .text("Time");

        svg.append("text")
                .attr("class", "axisLabel")
                .attr("transform", "rotate(90)")
                .attr("y", -40)
                .attr("x", (height / 2))
                .style("text-anchor", "middle")
                .text("Luminosity");

        //Append timeseries points
        svg.selectAll("dot")
                .data(data)
                .enter().append("circle")
                .attr("class", "dot")
                .attr("r", 1)
                .attr("cx", function (d) {
                    return x(d.time);
                })
                .attr("cy", function (d) {
                    return y(d.lum);
                });
                
        svg.call(zoom);
        
        //Zoom function
        function zoomed() {
            view.attr("transform", d3.event.transform);
            gX.call(xAxis.scale(d3.event.transform.rescaleX(x)));
            gY.call(yAxis.scale(d3.event.transform.rescaleY(y)));
            svg.selectAll(".dot").attr("transform", d3.event.transform);
        }

    });

} //End renderTimeseries
