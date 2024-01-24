<jsp:include page='/MasterPageTopSection.jsp' />
<script  src='jquery-3.1.1/jquery-3.1.1.min.js'></script>
<script src='js/addItem.js'></script>
<h1>Item (Add Module)</h1>
<h3>JSON Example</h3>
<form id='itemAddForm' >
<table border='1'>
<tr><td>NAME</td>
<td><input type='text' name='name' id='name' maxlength='30' size='30' >
<span id='nameErrorSection'></span></td></tr>
<tr><td>CATEGORY</td>
<td><input type='text' name='category' id='category' maxlength='30' size='30' >
<span id='categoryErrorSection'></span></td></tr>
<tr><td>PRICE</td>
<td><input type='number' name='price' id='price' maxlength='30' size='30' >
<span id='priceErrorSection'></span></td></tr>
<tr><td colspan='2' align='center'><button type='button' value='SAVE' onclick='addItem()' ></button></td></tr>
</table>
</form>
<jsp:include page='/MasterPageBottomSection.jsp' />