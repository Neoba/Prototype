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

//usernamelistview
function (doc, meta) {
  if(doc.type=="user")
  	emit(doc.username, null);
}

//usertoid
function (doc, meta) {
  if(doc.type=="user")
  	emit(doc.username, meta.id);
}

//usernametofacebookid
function (doc, meta) {
  if(doc.type=="user")
  {
    emit(doc.username,doc.facebook_id);
  }
  
}

//facebookidtoid
function (doc, meta) {
  if(doc.type=="user")
  {
    emit(doc.facebook_id,{"id":meta.id,"username":doc.username});
  }
  
}