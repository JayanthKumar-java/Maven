// [[${DeclarationDate}]]


function TableInsertion(){
for(let i=0;i<5;i++){
    let table = document.getElementById("myTable");
    let row=table.insertRow(-1);
     let c1 = row.insertCell(0);
      
     c1.innerText = "Elon"    
 
}
}

TableInsertion();
