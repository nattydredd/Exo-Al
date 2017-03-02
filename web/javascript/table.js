function renderTable(data, targetElement) {
    
    var columns = data.columnLabels;
//var columns = ["Star ID","Count","Class val 1","Class val 2","Class val 3","Class val 4","Class val 5","Total"];
    var rowData = data.rowData;
    console.log("Columns " + columns);
    console.log("Rows " + rowData);
    var table = d3.select(targetElement).append('table');
    var thead = table.append('thead');
    var tbody = table.append('tbody');

    // append the header row
    thead.append('tr')
            .selectAll('th')
            .data(columns).enter()
            .append('th')
            .text(function (column) {
                return column;
            });

    // create a row for each object in the data
    var rows = tbody.selectAll('tr')
            .data(rowData)
            .enter()
            .append('tr');

    // create a cell in each row for each column
    var cells = rows.selectAll('td')
            .data(function (row) {
                return columns.map(function (column) {
                    return {column: column, value: row[column]};
                });
            })
            .enter()
            .append('td')
            .text(function (d) {
                return d.value;
            });

}