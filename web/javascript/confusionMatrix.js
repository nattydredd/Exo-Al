function renderConfusionMatrix(data, targetElement) {

    //Set dimensions
    var margin = {top: 50, right: 50, bottom: 75, left: 100};
    var width = 250,
            height = 250;

    var legendWidth = 100;

    //Color scale
    //Green and grey
    var startColor = "#3a3a3a";
    var endColor = "#25d400";

    //Blue and grey
//    var startColor = "#3a3a3a";
//    var endColor = "#22b3eb";

    //Labels
    var labelsData = ["Non-host", "Host"];

    //Validate matrix
    if (!Array.isArray(data) || !data.length || !Array.isArray(data[0])) {
        throw new Error('It should be a 2-D array');
    }

    //Get min max data values
    var maxValue = d3.max(data, function (layer) {
        return d3.max(layer, function (d) {
            return d;
        });
    });
    var minValue = d3.min(data, function (layer) {
        return d3.min(layer, function (d) {
            return d;
        });
    });

    //Get number of rows and columns
    var numRows = data.length;
    var numCols = data[0].length;

    //Get scale for colum/row values
    var x = d3.scaleBand()
            .domain(d3.range(numCols))
            .range([0, width]);

    var y = d3.scaleBand()
            .domain(d3.range(numRows))
            .range([0, height]);

    //Get scale for color
    var colorMap = d3.scaleLinear()
            .domain([minValue, maxValue])
            .range([startColor, endColor]);

    //Append confusion matrix svg
    var svg = targetElement.append("svg")
            .attr("class", "confusionMatrix")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    //Confusion matrix border
    var border = svg.append("rect")
            .attr("class", "confusionMatrixBorder")
            .attr("width", width)
            .attr("height", height);

    //Append row cells
    var row = svg.selectAll(".row")
            .data(data)
            .enter().append("g")
            .attr("class", "row")
            .attr("transform", function (d, i) {
                return "translate(0," + y(i) + ")";
            });

    var cell = row.selectAll(".cell")
            .data(function (d) {
                return d;
            })
            .enter().append("g")
            .attr("class", "cell")
            .attr("transform", function (d, i) {
                return "translate(" + x(i) + ", 0)";
            });

    cell.append('rect')
            .attr("width", x.bandwidth())
            .attr("height", y.bandwidth())
            .style("stroke-width", 0);

    //Append data as labels
    cell.append("text")
            .attr("dy", ".32em")
            .attr("x", x.bandwidth() / 2)
            .attr("y", y.bandwidth() / 2)
            .attr("text-anchor", "middle")
            .style("fill", function (d, i) {
                return d >= maxValue / 2 ? 'white' : 'black';
            })
            .text(function (d, i) {
                return d;
            });

    //Color cells
    row.selectAll(".cell")
            .data(function (d, i) {
                return data[i];
            })
            .style("fill", colorMap);

    //Append outer labels
    var labels = svg.append('g')
            .attr('class', "confusionMatrixLabels");

    var columnLabels = labels.selectAll(".column-label")
            .data(labelsData)
            .enter().append("g")
            .attr("class", "column-label")
            .attr("transform", function (d, i) {
                return "translate(" + x(i) + "," + height + ")";
            });

    columnLabels.append("line")
            .style("stroke", "black")
            .style("stroke-width", "1px")
            .attr("x1", x.bandwidth() / 2)
            .attr("x2", x.bandwidth() / 2)
            .attr("y1", 0)
            .attr("y2", 5);

    columnLabels.append("text")
            .attr("x", 30)
            .attr("y", y.bandwidth() / 2)
            .attr("dy", ".22em")
            .attr("text-anchor", "end")
            .attr("transform", "rotate(-60)")
            .text(function (d, i) {
                return d;
            });

    var rowLabels = labels.selectAll(".row-label")
            .data(labelsData)
            .enter().append("g")
            .attr("class", "row-label")
            .attr("transform", function (d, i) {
                return "translate(" + 0 + "," + y(i) + ")";
            });

    rowLabels.append("line")
            .style("stroke", "black")
            .style("stroke-width", "1px")
            .attr("x1", 0)
            .attr("x2", -5)
            .attr("y1", y.bandwidth() / 2)
            .attr("y2", y.bandwidth() / 2);

    rowLabels.append("text")
            .attr("x", -8)
            .attr("y", y.bandwidth() / 2)
            .attr("dy", ".32em")
            .attr("text-anchor", "end")
            .text(function (d, i) {
                return d;
            });

    //Append legend svg
    var key = targetElement.append("svg")
            .attr("class", "confusionMatrixLegend")
            .attr("width", legendWidth)
            .attr("height", height + margin.top + margin.bottom);

    //Key border
    var keyBorder = key.append("rect")
            .attr("class", "confusionMatrixBorder")
            .attr("width", legendWidth / 2 - 10)
            .attr("height", height)
            .attr("transform", "translate(3," + margin.top + ")");

    //Gradient
    var legend = key.append("defs")
            .append("svg:linearGradient")
            .attr("id", "gradient")
            .attr("x1", "100%")
            .attr("y1", "0%")
            .attr("x2", "100%")
            .attr("y2", "100%")
            .attr("spreadMethod", "pad");

    legend.append("stop")
            .attr("offset", "0%")
            .attr("stop-color", endColor)
            .attr("stop-opacity", 1);

    legend.append("stop")
            .attr("offset", "100%")
            .attr("stop-color", startColor)
            .attr("stop-opacity", 1);

    key.append("rect")
            .attr("width", legendWidth / 2 - 10)
            .attr("height", height)
            .style("fill", "url(#gradient)")
            .attr("transform", "translate(3," + margin.top + ")");

    //Axis
    var y = d3.scaleLinear()
            .range([height, 0])
            .domain([minValue, maxValue]);

    var yAxis = d3.axisRight()
            .scale(y);

    key.append("g")
            .attr("class", "y axis")
            .attr("transform", "translate(44," + margin.top + ")")
            .call(yAxis);

}//End renderConfusionMatrix