<table id="GROUPTable">
    <thead>
      <tr>
        <%
          List<String> headers = (List<String>) request.getAttribute("headers");
          for (String header : headers) {
        %>
        <th><%= header %></th>
        <%
          }
        %>
      </tr>
    </thead>
    <tbody>
      <%
        int numRows = (int) request.getAttribute("numRows");
        List<List<String>> cells = (List<List<String>>) request.getAttribute("cells");
        for (int i = 0; i < numRows; i++) {
      %>
      <tr>
        <%
          List<String> rowCells = cells.get(i);
          for (String cell : rowCells) {
        %>
        <td style="vertical-align: top;"><%= cell %></td>
        <%
          }
        %>
      </tr>
      <%
        }
      %>
    </tbody>
  </table>