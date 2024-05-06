/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.runtime.test.impl;

import org.pentaho.runtime.test.RuntimeTest;
import org.pentaho.runtime.test.RuntimeTestProgressCallback;
import org.pentaho.runtime.test.RuntimeTester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bryan on 8/12/15.
 */
public class RuntimeTesterImpl implements RuntimeTester {
  private final List<RuntimeTest> runtimeTests;
  private final ExecutorService executorService;
  private final RuntimeTestRunner.Factory runtimeTestRunnerFactory;
  private RuntimeTestComparator runtimeTestComparator;
  private static RuntimeTesterImpl instance;

  public RuntimeTesterImpl( List<RuntimeTest> runtimeTests, ExecutorService executorService,
                            String orderedModulesString ) {
    this( runtimeTests, executorService, orderedModulesString, new RuntimeTestRunner.Factory() );
  }

  public static RuntimeTester getInstance(){
    if ( instance == null ) {
      //ToDo: populate runtimeTests with about 13 classes implementing RuntimeTest
      List<RuntimeTest> runtimeTests = new ArrayList<>();
      //runtimeTests.add( new ???)

      instance = new RuntimeTesterImpl( runtimeTests, Executors.newCachedThreadPool(), "Hadoop Configuration,Hadoop File System,Map Reduce,Oozie,Zookeeper" );
    }
    return instance;
  }

  public RuntimeTesterImpl( List<RuntimeTest> runtimeTests, ExecutorService executorService,
                            String orderedModulesString, RuntimeTestRunner.Factory runtimeTestRunnerFactory ) {
    this.runtimeTests = runtimeTests;
    this.executorService = executorService;
    this.runtimeTestRunnerFactory = runtimeTestRunnerFactory;
    HashMap<String, Integer> orderedModules = new HashMap<>();
    String[] split = orderedModulesString.split( "," );
    for ( int module = 0; module < split.length; module++ ) {
      orderedModules.put( split[ module ].trim(), module );
    }
    runtimeTestComparator = new RuntimeTestComparator( orderedModules );
  }

  @Override
  public void runtimeTest( final Object objectUnderTest,
                           final RuntimeTestProgressCallback runtimeTestProgressCallback ) {
    final List<RuntimeTest> runtimeTests = new ArrayList<>( this.runtimeTests );
    Collections.sort( runtimeTests, runtimeTestComparator );
    executorService.submit( new Runnable() {
      @Override public void run() {
        runtimeTestRunnerFactory.create( runtimeTests, objectUnderTest, runtimeTestProgressCallback, executorService )
          .runTests();
      }
    } );
  }
}
