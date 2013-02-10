function onStartup()
{
	local.write("{\"friends\":{\"a1\":\"9020\"}}")
	
	friends = eval("("+local.navigate("friends").toJsonString()+")")
	println(friends)
	for(friend in friends)
	{
		println(friend+"->"+friends[friend])
		remote.send(friends[friend],"handleCheckin",0)
	}
}

function handleCheckin() {

}

onStartup()