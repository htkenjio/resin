/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
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
 * @author Sam
 */

package com.caucho.config.types;

import com.caucho.bytecode.*;
import com.caucho.config.j2ee.*;
import com.caucho.ejb.*;
import com.caucho.ejb.cfg.EjbConfig;
import com.caucho.ejb.manager.EjbContainer;
import com.caucho.ejb.protocol.EjbProtocolManager;
import com.caucho.naming.Jndi;
import com.caucho.naming.ObjectProxy;
import com.caucho.util.BeanUtil;
import com.caucho.util.L10N;
import com.caucho.vfs.JarPath;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for the ejb-ref.
 *
 * An ejb-ref is used to make an ejb available within the environment
 * in which the ejb-ref is declared.
 */
public class EjbRef extends BaseRef implements ObjectProxy {
  private static final L10N L = new L10N(EjbRef.class);
  private static final Logger log
    = Logger.getLogger(EjbRef.class.getName());

  private Context _context;

  private String _ejbRefName;
  private String _type;
  private Class _home;
  private Class _remote;
  private String _foreignName;
  private String _ejbLink;

  private String _typeName;

  private Object _target;

  private boolean _isInitBinding;

  private String _clientClassName;

  public EjbRef()
  {
  }

  public EjbRef(Context context)
  {
    _context = context;
  }

  public EjbRef(Path modulePath)
  {
    super(modulePath);
  }


  public EjbRef(Path modulePath, String sourceEjbName)
  {
    super(modulePath, sourceEjbName);
  }

  public boolean isEjbLocalRef()
  {
    return false;
  }

  /**
   * Gets the injection-target
   */
  public InjectionTarget getInjectionTarget()
  {
    return _injectionTarget;
  }

  public Class getLocal()
  {
    return null;
  }

  protected String getTagName()
  {
    return "<ejb-ref>";
  }

  public void setId(String id)
  {
  }

  public void setDescription(String description)
  {
  }

  public void setClientClassName(String clientClassName)
  {
    _clientClassName = clientClassName;
  }

  /**
   * Sets the name to use in the local jndi context.
   * This is the jndi lookup name that code uses to obtain the home for
   * the bean when doing a jndi lookup.
   *
   * <pre>
   *   <ejb-ref-name>ejb/Gryffindor</ejb-ref-name>
   *   ...
   *   (new InitialContext()).lookup("java:comp/env/ejb/Gryffindor");
   * </pre>
   */
  public void setEjbRefName(String name)
  {
    _ejbRefName = name;
  }

  /**
   * Sets the injection-target
   */
  public void setInjectionTarget(InjectionTarget injectionTarget)
  {
    _injectionTarget = injectionTarget;
  }

  /**
   * Returns the ejb name.
   */
  public String getEjbRefName()
  {
    return _ejbRefName;
  }

  public void setEjbRefType(String type)
  {
    _type = type;
  }

  public void setHome(Class home)
  {
    _home = home;
  }

  /**
   * Returns the home class.
   */
  public Class getHome()
  {
    return _home;
  }

  public void setRemote(Class remote)
  {
    _remote = remote;
  }

  /**
   * Returns the remote class.
   */
  public Class getRemote()
  {
    // XXX: should distinguish
    return _remote;
  }

  /**
   * Sets the canonical jndi name to use to find the bean that
   * is the target of the reference.
   * For remote beans, a &lt;jndi-link> {@link com.caucho.naming.LinkProxy} is
   * used to link the local jndi context referred to in this name to
   * a remote context.
   */
  public void setForeignName(String foreignName)
  {
    _foreignName = foreignName;
  }

  /**
   * Set the target of the reference, an alternative to {@link #setJndiName(String)}.
   * The format of the ejbLink is "bean", or "jarname#bean", where <i>bean</i> is the
   * ejb-name of a bean within the same enterprise application, and <i>jarname</i>
   * further qualifies the identity of the target.
   */
  public void setEjbLink(String ejbLink)
  {
    _ejbLink = ejbLink;
  }

  /**
   * Merges duplicated information in application-client.xml / resin-application-client.xml
   */
  public void mergeFrom(EjbRef other)
  {
    if (_foreignName == null)
      _foreignName = other._foreignName;

    if (_ejbLink == null)
      _ejbLink = other._ejbLink;

    if (_type == null)
      _type = other._type;

    if (_home == null)
      _home = other._home;

    if (_remote == null)
      _remote = other._remote;

    if (_injectionTarget == null)
      _injectionTarget = other._injectionTarget;
  }

  // XXX TCK, needs QA
  @PostConstruct
  public void init()
    throws Exception
  {
    // TCK, needsQA, ejb30/bb/session/stateless/sessioncontext/descriptor/getBusinessObjectLocal1

    // Cannot do initialization here as there might be duplicated ejb-ref's in
    // application-client.xml and resin-application-client.xml or even additional
    // configuration files (TCK).
    // application-client: <ejb-ref> initialization
    // if (_clientClassName != null)
    //  initBinding(null);

    if (log.isLoggable(Level.FINER))
      log.log(Level.FINER, L.l("{0} init", this));
  }

  // XXX TCK, needs QA @PostConstruct, called from EjbConfig.deployBeans()
  public void initBinding(AbstractServer ejbServer)
    throws Exception
  {
    if (_isInitBinding)
      return;

    _isInitBinding = true;

    if (_foreignName != null) {
      int pos = _foreignName.indexOf("#");

      // TCK/IIOP for multiple interfaces 2.1/3.0 home/remote, needs QA
      // TCK: ejb30/bb/session/stateful/sessioncontext/descriptor/getBusinessObjectLocal1
      if (pos < 0) {
        // TCK, needs QA: ejb30/bb/session/stateless/migration/twothree/descriptor
        // The EJB 2.1 home is bound to the foreign name. No need to append the interface.
        if (_home == null) {
          if (_remote != null)
            _foreignName += "#" + _remote.getName().replace(".", "_");
          else if (getLocal() != null)
            _foreignName += "#" + getLocal().getName().replace(".", "_");
        }
      }
    }

    EjbRefContext context = EjbRefContext.createLocal();

    context.add(this);

    boolean bind = false;

    EjbContainer container = EjbContainer.getCurrent();
    EjbProtocolManager protocolManager = null;

    if (container != null)
      protocolManager = container.getProtocolManager();

    String fullEjbRefName = Jndi.getFullName(_ejbRefName);

    /*
    if (isEjbLocalRef()) {
      if (server == null || server.getLocalJndiPrefix() == null)
        fullEjbRefName = Jndi.getFullName(_ejbRefName);
      else // ejb/0gc5
        fullEjbRefName = Jndi.getFullName(server.getLocalJndiPrefix()
                                          + "/" + _ejbRefName);
    } else {
      if (server == null || server.getRemoteJndiPrefix() == null)
        fullEjbRefName = Jndi.getFullName(_ejbRefName);
      else // ejb/0gc5
        fullEjbRefName = Jndi.getFullName(server.getRemoteJndiPrefix()
                                          + "/" + _ejbRefName);
    }
    */

    Object targetValue = null;

    if (_ejbLink != null) {
      String fullEjbLink = null;

      int pos = _ejbLink.indexOf("#");

      if (pos > 0) {
        // XXX TCK
        fullEjbLink = _foreignName;
      }
      else if (isEjbLocalRef()) {
        if (protocolManager == null || protocolManager.getLocalJndiPrefix() == null)
          fullEjbLink = Jndi.getFullName(_ejbLink);
        else
          fullEjbLink = Jndi.getFullName(protocolManager.getLocalJndiPrefix()
                                         + "/" + _ejbLink);
      }
      else {
        if (protocolManager == null || protocolManager.getRemoteJndiPrefix() == null)
          fullEjbLink = Jndi.getFullName(_ejbLink);
        else {
          fullEjbLink = Jndi.getFullName(protocolManager.getRemoteJndiPrefix()
                                         + "/" + _ejbLink);
        }
      }

      if (_injectionTarget != null && fullEjbLink != null) {
        targetValue = Jndi.lookup(fullEjbLink);
      }

      bind = true;
    }
    else if (_foreignName != null) {
      String fullForeignName = Jndi.getFullName(_foreignName);

      if (! fullForeignName.equals(fullEjbRefName))
        bind = true;
    }
    else
      bind = true;

    if (bind) {
      try {
        Object value = Jndi.lookup(fullEjbRefName);

        if (value != null)
          bind = false;
      } catch (Exception e) {
      }

      try {
        if (bind)
          Jndi.rebindDeep(fullEjbRefName, this);
      } catch (Exception e) {
        throw e;
      }
    }

    // XXX TCK, needs QA
    if (_injectionTarget != null && targetValue != null) {
      String className = _injectionTarget.getInjectionTargetClass();
      String fieldName = _injectionTarget.getInjectionTargetName();

      AccessibleInject accessibleInject = null;

      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      JClassLoader jClassLoader = JClassLoaderWrapper.create(loader);

      JClass jClass = jClassLoader.forName(className);

      Class cl = jClass.getJavaClass();

      Method method = BeanUtil.getSetMethod(cl, fieldName);

      /*
      if (method != null)
        accessibleInject = new PropertyInject(method);
      else {
        Field field = cl.getDeclaredField(fieldName);
        accessibleInject = new FieldInject(field);
      }

      EjbServerManager manager = EjbServerManager.getLocal();

      if (manager != null) {
        Class type;

        if (isEjbLocalRef())
          type = getLocal();
        else
          type = getRemote();

        EjbInjectProgram program = new EjbInjectProgram(_ejbRefName,
                                                        null,
                                                        ejbServer.getMappedName(),
                                                        type,
                                                        accessibleInject);

        ejbServer.getInitProgram().addProgram(program);
      }
      else if (_clientClassName != null) {
        if (targetValue != null)
          accessibleInject.inject(null, targetValue);
      }
      */
    }
  }

  /**
   * Creates the object from the proxy.
   *
   * @return the object named by the proxy.
   */
  public Object createObject(Hashtable env)
    throws NamingException
  {
    if (_target == null) {
      // ejb/0f6g, TCK
      if (_foreignName != null)
        resolve(null);
      else if (_home != null)
        resolve(_home);
      else if (_remote != null)
        resolve(_remote);
      else if (getLocal() != null) // ejb/0f6g
        resolve(getLocal());
      else
        resolve(null);
    }

    return _target;
  }

  public Object getByType(Class type)
  {
    try {
      if (_home != null && type.isAssignableFrom(_home))
        return createObject(null);

      if (_remote != null && type.isAssignableFrom(_remote))
        return createObject(null);

      if (_foreignName != null) {
        int pos = _foreignName.indexOf("#");

        if (pos > 0) {
          String intf = _foreignName.substring(++pos).replace("_", ".");

          // TCK: application-client.xml with multiple business interfaces.
          if (! type.getName().equals(intf))
            return null;
        }

        Object target;

        // XXX: JDK's iiop lookup
        String foreignName = _foreignName.replace('.', '_');

        if (_context != null) {
          target = _context.lookup(foreignName);
        } else {
          target = Jndi.lookup(foreignName);
        }

        if (target != null && type != null)
          return PortableRemoteObject.narrow(target, type);
      }
    } catch (Exception e) {
      // log.log(Level.FINER, e.toString(), e);
    }

    return null;
  }

  private void resolve(Class type)
    throws NamingException
  {
    if (log.isLoggable(Level.FINEST))
      log.log(Level.FINEST, L.l("{0} resolving", this));

    if (_foreignName != null)
      _target = lookupByForeignJndi(_foreignName, type);
    else if (_ejbLink != null)
      _target = lookupByLink(_ejbLink, type);
    else
      _target = lookupLocal(type);

    if (log.isLoggable(Level.CONFIG))
      log.log(Level.CONFIG, L.l("{0} resolved", this));
  }

  private Object lookupByLink(String link, Class type)
    throws NamingException
  {
    Object target = null;

    String archiveName;
    String ejbName;

    int hashIndex = link.indexOf('#');

    if (hashIndex < 0) {
      archiveName = null;
      ejbName = link;
    }
    else {
      archiveName = link.substring(0, hashIndex);
      ejbName = link.substring(hashIndex + 1);
    }

    try {
      Path path = archiveName == null ? _modulePath : _modulePath.lookup(archiveName);

      if (true)
	throw new IllegalStateException();
      /*
      EJBServer ejbServer = EJBServer.getLocal();
      AbstractServer server = null;

      if (ejbServer != null) {
        server = ejbServer.getServer(path, ejbName);

        if (server == null && archiveName == null)
          server = ejbServer.getServer(ejbName);
      }

      if (server != null) {
        if (isEjbLocalRef()) {
          target = server.getEJBLocalHome();

          if (target != null) {
            // ejb/0f6g
            if (type != null && ! type.isAssignableFrom(target.getClass()))
              target = null;
          }

          if (target == null) {
            target = server.getLocalObject(type);
          }
        } else {
          target = server.getEJBHome();

          if (target != null) {
            if (type != null && ! type.isAssignableFrom(target.getClass()))
              target = null;
          }

          if (target == null) {
            target = server.getRemoteObject(type);
          }
        }

        if (target == null) {
          log.log(Level.FINE, L.l("no home or business interface is available for '{0}'", server));

          throw new NamingException(L.l("{0} '{1}' ejb bean found with ejb-link '{2}' has no home or business interface",
                                        getTagName(), _ejbRefName, link));
        }
      }
      else {
        target = lookupByForeignJndi(_ejbLink, type);
      }

      if (target != null && target instanceof ObjectProxy) {
        ObjectProxy proxy = (ObjectProxy) target;
        target = proxy.createObject(null);
      }
      */

      if (false) throw new NamingException();
    }
    catch (NamingException e) {
      throw e;
    }
    catch (Exception e) {
      log.log(Level.FINER, e.toString(), e);
      throw new NamingException(L.l("{0} '{1}'  ejb-link '{2}' invalid ",
                                    getTagName(), _ejbRefName, link));
    }

    return target;
  }

  private Object lookupByForeignJndi(String foreignName, Class type)
    throws NamingException
  {
    Object target = Jndi.lookup(foreignName);

    return target;

    /* XXX
    Object target = null;

    String fullForeignName = Jndi.getFullName(foreignName);
    String fullEjbName = Jndi.getFullName(_ejbRefName);

    if (_context != null) {
      target = _context.lookup(fullForeignName);
    }
    else if (fullForeignName.equals(fullEjbName)) {
      // ejb/0ga0
      return null;
    }
    else
      target = Jndi.lookup(foreignName);

    if (target == null) {
      if (fullForeignName.equals(fullEjbName))
        throw new NamingException(L.l("{0} '{1}' cannot be resolved",
                                      getTagName(), _ejbRefName));
      else
        throw new NamingException(L.l("{0} '{1}' foreign-name '{2}' not found",
                                      getTagName(), _ejbRefName, foreignName));
    }

    if (type != null)
      target = PortableRemoteObject.narrow(target, type);

    return target;
    */
  }

  private Object lookupLocal(Class type)
  {
    EjbServerManager manager = EjbServerManager.getLocal();

    if (manager != null) {
      Object value = manager.getLocalByInterface(type);

      if (value instanceof ObjectProxy) {
        try {
          return ((ObjectProxy) value).createObject(null);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      else if (value != null)
        return value;
      else
        return manager.getRemoteByInterface(type);
    }

    /*
      EjbRefContext context = EjbRefContext.getLocal();

      if (context != null)
        return context.findByType(type);
    */

    return null;
  }

  public String toString()
  {
    return getClass().getSimpleName()
      +  "[" + _ejbRefName + ", " + _ejbLink + ", " +  _foreignName + "]";
  }
}
