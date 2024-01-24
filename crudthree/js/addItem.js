function jsonToQueryString(json)
{
alert($);
return "?"+Object.keys(json).map(function(key){
return encodeURIComponent(key)+"="+encodeURIComponent(json[key]);
}).join("&");
}
function addItem()
{
var json={
"name" : $("#name").val(),
"category" : $("#category").val(),
"price" : $("#price").val(),
};
var qs=jsonToQueryString(json);
alert($);
$.ajax(
{
"type" : "GET",
"url" : 'addItem'+qs,
"success" : function(data){
alert(data);
if(data.success==true)
{
alert("deepesh43");
$("#notificationSection").html("data added with code : "+data.code);
}
else
{
alert("deepesh54");
$("#notificationSection").html("unable to add error is  : "+data.exception);
}
},
"error" : function(){
alert("deepesh654");
alert();
}
});
}