function onStartup()
{
	println("looking for friends + "+id)
	
	println ("yes: "+local.getRootNode().toJsonString());
	
	if(local.getRootNode().navigate("friends").exists()) {
		
		friends = eval("("+local.getRootNode().navigate("friends").toJsonString()+")")
		println(friends)
		for(friend in friends)
		{
			println(friend+"->"+friends[friend])
			remote.send(friends[friend],"handleCheckin",0)
		}
	}
}

function handleCheckin() {
	println("in handleCheckin + "+id)
}
