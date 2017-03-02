function renderTable(data, targetElement) {

    //Separate column labels and row data
    var columns = data.columnLabels;
    var rowData = data.rowData;

    //Append table header and body to target element
    var table = d3.select(targetElement).append('table');
    var thead = table.append('thead');
    var tbody = table.append('tbody');

    //Append the header row
    thead.append('tr')
            .selectAll('th')
            .data(columns).enter()
            .append('th')
            .text(function (column) {
                return column;
            });

    //Create a row for each object in the data
    var rows = tbody.selectAll('tr')
            .data(rowData)
            .enter()
            .append('tr');

    //Create a cell in each row for each column
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