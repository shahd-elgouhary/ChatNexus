package com.chatroom.server;

import com.chatroom.network.StreamManager;

import com.chatroom.models.Response;

public class ResponseHolder {
	Response response;
	StreamManager streamManager;

	public ResponseHolder(Response r, StreamManager s) {
		response = r;
		streamManager = s;
	}
}
