function renderPieChart(data, targetElement) {

    //Set dimensions
    var width = 350,
            height = 350,
            radius = Math.min(width - 50, height - 50) / 2;

    //Color scale
    var color = d3.scaleOrdinal()
            .range(["#22b3eb", "#3a3a3a"]);

    //Size of arcs
    var arc = d3.arc()
            .outerRadius(radius - 5)
            .innerRadius(radius - 100);

    //Chart data
    var pie = d3.pie()
            .sort(null)
            .value(function (d) {
                return d.value;
            });

    //Append pie chart svg
    var svg = targetElement.append("svg")
            .attr("class", "pieChart")  
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(" + (width / 2) + "," + (height / 2) + ")")
            .attr("class", "pieChart");

    //Append arcs and paths
    var arcs = svg.selectAll(".arc")
            .data(pie(data))
            .enter().append("g")
            .attr("class", "arc");

    arcs.append("path")
            .attr("d", arc)
            .style("fill", function (d) {
                return color(d.data.label);
            })
            //Create a new invisible arc that the text can flow along
            .each(function (d, i) {
                var firstArcSection = /(^.+?)L/;
                var newArc = firstArcSection.exec(d3.select(this).attr("d"))[1];
                newArc = newArc.replace(/,/g, " ");

                svg.append("path")
                        .attr("class", "hiddenArcs")
                        .attr("id", "hiddenArc" + i)
                        .attr("d", newArc)
                        .style("fill", "none");
            });

    //Append the label names on the outside
    svg.selectAll(".pieChartLabel Outer")
            .data(data)
            .enter().append("text")
            .attr("class", "pieChartLabelOuter")
            .attr("dy", -13)
            .append("textPath")
            .attr("startOffset", "50%")
            .attr("xlink:href", function (d, i) {
                return "#hiddenArc" + i;
            })
            .text(function (d) {
                return d.label;
            });

    //Append the label values on the inside
    arcs.append("text")
            .attr("class", "pieChartLabelInner")
            .attr("transform", function (d) {
                return "translate(" + arc.centroid(d) + ")";
            })
            .attr("dy", ".35em")
            .text(function (d) {
                return d.data.value + "%";
            });

}//End renderPieChart
