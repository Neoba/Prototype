//created_docs
function (doc, meta) {
  if(doc.type=="document")
  	emit(doc.creator, meta.id);
}

//idsview
function (doc, meta) {
  if(doc.type=="document")
  	emit(meta.id,doc, null);
}

//usertoid
function (doc, meta) {
  if(doc.type=="user")
  	emit(doc.username, meta.id);
}
