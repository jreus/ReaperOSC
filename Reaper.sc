/******************************************

Control Reaper from SuperCollider

2018 Jonathan Reus
jonathanreus.com
info@jonathanreus.com

*******************************************/



/* USAGE: *********************

r = Reaper.new("localhost", 8000);




********************************/


Reaper {
	var <tracksById, <tracksByName;
	var oscfunc;
	var <reaperAddr;

	*new {arg host="localhost", port=8000;
		^super.new.init(host, port);
	}

	send {arg addr, val=nil;
		"Sending Msg: % %".postf(addr, val);
		reaperAddr.sendMsg(addr,val);
	}


	init {arg host, port;
		//tracksById = Array.newClear(64);
		tracksById = Dictionary.new;
		tracksByName = Dictionary.new;
		reaperAddr = NetAddr(host, port);

		// Set up the OSC listener
		oscfunc = {arg msg, time, addr;
			var matches, tnum, track, cmd, val;

			msg.postln;
			matches = msg[0].asString.findRegexp("(/track/([0-9][0-9]?))[/]?([/A-Za-z0-9]*)");

			if(matches.size > 2) {
				tnum = matches[2][1].asInt;
				cmd = matches[3][1];
				val = msg[1];
				// If a track doesn't exist by name or by number, create it
				// parse the message, but let the track do that..
				track = tracksById[tnum];
				if(track.isNil) {
					track = ReaperTrack.new(tnum, this);
					tracksById[tnum] = track;
				};
				track.parseCmd(cmd, val);

			};

		};

		thisProcess.addOSCRecvFunc(oscfunc);
	}




}




ReaperTrack {
	var <params;
	var fxunits;
	var setup;

	*new {arg number, parentsetup;
		^super.new.init(number, parentsetup);
	}

	init {arg number, parentsetup;
		setup = parentsetup;
		params = Dictionary.new;
		params["number"] = number;
		fxunits = Dictionary.new;
	}

	parseCmd {arg cmd, val;
		if(cmd == "volume") {
			params[cmd] = val;
		};
		if(cmd == "name") {
			params[cmd] = val;
			setup.tracksByName[val] = this;
		};
	}

	volume_ {arg val;
		var addr;
		val = val.clip(0,1);
		params["volume"] = val;
		addr = "/track/%/volume".format(params["number"]);
		setup.send(addr, val);
	}


}

ReaperFX {
	var params;
	var parentTrack;
}

