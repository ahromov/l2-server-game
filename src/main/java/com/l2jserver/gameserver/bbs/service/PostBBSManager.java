/*
 * Copyright © 2004-2020 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.bbs.service;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.bbs.model.Forum;
import com.l2jserver.gameserver.bbs.model.ForumType;
import com.l2jserver.gameserver.bbs.model.Post;
import com.l2jserver.gameserver.bbs.model.Topic;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.handler.CommunityBoardHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.util.StringUtil;

/**
 * Post BBS Manager.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class PostBBSManager extends BaseBBSManager {
	
	private final Map<Topic, List<Post>> _postByTopic = new ConcurrentHashMap<>();
	
	protected PostBBSManager() {
		// Do nothing.
	}
	
	public List<Post> getGPostByTopic(Topic topic) {
		var posts = _postByTopic.get(topic);
		if (posts == null) {
			posts = DAOFactory.getInstance().getPostRepository().load(topic);
			_postByTopic.put(topic, posts);
		}
		return posts;
	}
	
	public void delPostByTopic(Topic t) {
		_postByTopic.remove(t);
	}
	
	public void addPostByTopic(Topic topic, List<Post> posts) {
		_postByTopic.putIfAbsent(topic, posts);
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar) {
		if (command.startsWith("_bbsposts;read;")) {
			final var st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final var forumId = Integer.parseInt(st.nextToken());
			final var topicId = Integer.parseInt(st.nextToken());
			final var index = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			
			final var forum = ForumsBBSManager.getInstance().getForumById(forumId);
			final var topic = TopicBBSManager.getInstance().getTopicById(topicId);
			showPost(topic, forum, activeChar, index);
		} else if (command.startsWith("_bbsposts;edit;")) {
			final var st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final var forumId = Integer.parseInt(st.nextToken());
			final var topicId = Integer.parseInt(st.nextToken());
			final var postId = Integer.parseInt(st.nextToken());
			
			final var forum = ForumsBBSManager.getInstance().getForumById(forumId);
			final var topic = TopicBBSManager.getInstance().getTopicById(topicId);
			showEditPost(topic, forum, activeChar, postId);
		} else {
			CommunityBoardHandler.separateAndSend("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", activeChar);
		}
	}
	
	private void showEditPost(Topic topic, Forum forum, L2PcInstance activeChar, int idp) {
		if (topic == null) {
			CommunityBoardHandler.separateAndSend("<html><body><br><br><center>Error: This topic does not exist!</center></body></html>", activeChar);
		} else {
			final List<Post> p = getGPostByTopic(topic);
			if ((forum == null) || (p == null)) {
				CommunityBoardHandler.separateAndSend("<html><body><br><br><center>Error: This forum or post does not exist!</center></body></html>", activeChar);
			} else {
				showHtmlEditPost(topic, activeChar, forum, p);
			}
		}
	}
	
	private void showPost(Topic topic, Forum forum, L2PcInstance activeChar, int ind) {
		if ((forum == null) || (topic == null)) {
			CommunityBoardHandler.separateAndSend("<html><body><br><br><center>Error: This forum is not implemented yet!</center></body></html>", activeChar);
		} else if (forum.getType() == ForumType.MEMO) {
			showMemoPost(topic, activeChar, forum);
		} else {
			CommunityBoardHandler.separateAndSend("<html><body><br><br><center>The forum: " + forum.getName() + " is not implemented yet!</center></body></html>", activeChar);
		}
	}
	
	private void showHtmlEditPost(Topic topic, L2PcInstance activeChar, Forum forum, List<Post> posts) {
		final String html = StringUtil.concat("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">", forum.getName(), " Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540>", topic.getName(), "</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post ", String.valueOf(forum.getId()), ";", String.valueOf(topic.getId()), ";0 _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>");
		send1001(html, activeChar);
		send1002(activeChar, posts.get(0).getTxt(), topic.getName(), DateFormat.getInstance().format(new Date(topic.getDate())));
	}
	
	private void showMemoPost(Topic topic, L2PcInstance activeChar, Forum forum) {
		final var posts = getGPostByTopic(topic);
		Locale locale = Locale.getDefault();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		
		String mes = posts.get(0).getTxt().replace(">", "&gt;");
		mes = mes.replace("<", "&lt;");
		// TODO(Zoey76): Replace this with a template.
		final String html = StringUtil.concat("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0 bgcolor=333333><tr><td height=10></td></tr><tr><td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td><td fixWIDTH=380 valign=top>", topic.getName(), "</td><td fixwidth=5></td><td fixwidth=50></td><td fixWIDTH=120></td></tr><tr><td height=10></td></tr><tr><td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td><td><font color=\"AAAAAA\">", topic.getOwnerName()
			+ "</font></td><td></td><td><font color=\"AAAAAA\">&$418; :</font></td><td><font color=\"AAAAAA\">", dateFormat.format(posts.get(0).getDate()), "</font></td></tr><tr><td height=10></td></tr></table><br><table border=0 cellspacing=0 cellpadding=0><tr><td fixwidth=5></td><td FIXWIDTH=600 align=left>", mes, "</td><td fixqqwidth=5></td></tr></table><br><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><img src=\"L2UI.squaregray\" width=\"610\" height=\"1\"><img src=\"L2UI.squareblank\" width=\"1\" height=\"5\"><table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=560 align=right><table border=0 cellspacing=0><tr><td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;", String.valueOf(forum.getId()), ";", String.valueOf(topic.getId()), ";0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;", String.valueOf(forum.getId()), ";", String.valueOf(topic.getId()), "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;", String.valueOf(forum.getId()), "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;</tr></table></td></tr></table><br><br><br></center></body></html>");
		CommunityBoardHandler.separateAndSend(html, activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar) {
		StringTokenizer st = new StringTokenizer(ar1, ";");
		int idf = Integer.parseInt(st.nextToken());
		int idt = Integer.parseInt(st.nextToken());
		int idp = Integer.parseInt(st.nextToken());
		
		Forum f = ForumsBBSManager.getInstance().getForumById(idf);
		if (f == null) {
			CommunityBoardHandler.separateAndSend("<html><body><br><br><center>the forum: " + idf + " does not exist !</center><br><br></body></html>", activeChar);
		} else {
			Topic t = f.getTopic(idt);
			if (t == null) {
				CommunityBoardHandler.separateAndSend("<html><body><br><br><center>the topic: " + idt + " does not exist !</center><br><br></body></html>", activeChar);
			} else {
				final var posts = getGPostByTopic(t);
				if (!posts.isEmpty()) {
					final Post post = posts.get(idp);
					if (post == null) {
						CommunityBoardHandler.separateAndSend("<html><body><br><br><center>the post: " + idp + " does not exist !</center><br><br></body></html>", activeChar);
					} else {
						post.setTxt(ar4);
						DAOFactory.getInstance().getPostRepository().update(post);
						parsecmd("_bbsposts;read;" + f.getId() + ";" + t.getId(), activeChar);
					}
				}
			}
		}
	}
	
	public static PostBBSManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final PostBBSManager INSTANCE = new PostBBSManager();
	}
}
