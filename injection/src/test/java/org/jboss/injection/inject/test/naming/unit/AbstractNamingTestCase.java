/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.injection.inject.test.naming.unit;

import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.descriptor.UrlBootstrapDescriptor;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.test.BaseTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

/**
 * AbstractNamingTestCase
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractNamingTestCase
{

   protected Context context;

   protected static MCServer server;

   @BeforeClass
   public static void setupServer() throws Exception
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      server = MCServerFactory.createServer(classLoader);
      List<BootstrapDescriptor> descriptors = server.getConfiguration().getBootstrapDescriptors();

      // Always setup naming.
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractNamingTestCase.class, "/conf/bootstrap/naming.xml")));

      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         server.start();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
   }

   @Before
   public void initializeContext() throws Exception
   {
      context = new InitialContext();
   }

   protected void assertContextValue(String jndiName, Object value) throws Exception
   {
      String actual = (String) context.lookup(jndiName);
      Assert.assertEquals(value, actual);
   }

   protected void assertNameNotFound(String name)
   {
      try
      {
         context.lookup(name);
         Assert.fail("The name should not be found in the context: " + name);
      } catch(NamingException expected)
      {
      }
   }

}
