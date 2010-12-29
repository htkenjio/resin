/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.bam.client;

import com.caucho.bam.actor.Actor;
import com.caucho.bam.actor.ActorSender;
import com.caucho.bam.actor.SimpleActorSender;
import com.caucho.bam.broker.Broker;
import com.caucho.bam.broker.PassthroughBroker;
import com.caucho.bam.mailbox.Mailbox;
import com.caucho.bam.mailbox.MultiworkerMailbox;
import com.caucho.bam.stream.ActorStream;

/**
 * HMTP client protocol
 */
public class LinkClient {
  private LinkConnectionFactory _linkFactory;
  
  private Actor _actor;

  private Broker _outboundBroker;
  private ActorSender _sender;
  
  public LinkClient(LinkConnectionFactory linkFactory, Actor actor)
  {
    if (linkFactory == null)
      throw new NullPointerException();
      
    if (actor == null)
      throw new NullPointerException();
    
    _linkFactory = linkFactory;
    _actor = actor;
    
    PassthroughBroker inboundBroker = new PassthroughBroker();
    PassthroughBroker outboundBroker = new PassthroughBroker();
    
    ActorStream inboundStream = actor.getActorStream();
    SimpleActorSender sender = new SimpleActorSender(inboundStream, 
                                                     outboundBroker);
    
    _sender = sender;
    
    Mailbox inboundMailbox = createInboundMailbox(sender.getActorStream(),
                                                  outboundBroker);
    
    inboundBroker.setMailbox(inboundMailbox);
    
    ActorStream outboundStream
      = new OutboundActorStream(_linkFactory, inboundBroker);
    
    Mailbox outboundMailbox = createOutboundMailbox(outboundStream,
                                                    inboundBroker);
    outboundBroker.setMailbox(outboundMailbox);
    
    actor.setMailbox(inboundMailbox);
    actor.setBroker(outboundBroker);
    
    _outboundBroker = outboundBroker;
  }
  
  public Broker getBroker()
  {
    return _outboundBroker;
  }
  
  public ActorSender getSender()
  {
    return _sender;
  }
  
  public void start()
  {
    
  }
  
  public void close()
  {
    
  }
  
  protected Mailbox createInboundMailbox(ActorStream inboundStream,
                                         Broker outboundBroker)
  {
    return new MultiworkerMailbox(inboundStream, outboundBroker, 1);
  }
  
  protected Mailbox createOutboundMailbox(ActorStream outboundStream,
                                          Broker inboundBroker)
  {
    return new MultiworkerMailbox(outboundStream, inboundBroker, 1);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _linkFactory + "," + _actor + "]";
  }
}