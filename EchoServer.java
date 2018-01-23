package com.edmund;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class EchoServer {

	private static class Init extends ChannelInitializer {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ch.pipeline().addLast(new EchoHandler());
		}
	}

	private static class EchoHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			try {
				Charset utf8 = CharsetUtil.UTF_8;
				String in = ((ByteBuf) msg).toString(utf8);
				String out = in.toUpperCase(); // Echo back
				ctx.writeAndFlush(Unpooled.copiedBuffer(out, utf8));
			} finally {
				ReferenceCountUtil.release(msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new Init())
			.bind(7657).sync()
			.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
