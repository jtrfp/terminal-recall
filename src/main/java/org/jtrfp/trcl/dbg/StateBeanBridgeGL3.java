/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.dbg;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GL3ES3;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GL4;
import javax.media.opengl.GL4ES3;
import javax.media.opengl.GL4bc;
import javax.media.opengl.GLArrayData;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLES1;
import javax.media.opengl.GLES2;
import javax.media.opengl.GLES3;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;

import com.jogamp.common.nio.PointerBuffer;

public class StateBeanBridgeGL3 implements GL3 {
    private final GL3 delegate;
    private GLStateBean bean;
    
    public StateBeanBridgeGL3(GL3 delegate){
	this(delegate,new GLStateBean());
    }
    
    public StateBeanBridgeGL3(GL3 delegate, GLStateBean bean){
	this.delegate=delegate;
	this.bean=bean;
    }
    
    private void glException(GLException e){
	System.err.println("GLException intercepted, dumping GL State. If you don't want this, remove the StateBeanBridgeGL3 delegate from your GL.");
	bean.dumpHumanReadableStateReport(System.err);
    }
    
    public GLStateBean getGLStateBean(){
	try{ return bean;} catch(GLException e){glException(e); throw e;}
    }
    
    private void unhandledState(Object object){
	throw new RuntimeException("Unhandled state. "+object);
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL()
     */
    public boolean isGL() {
	try{ return delegate.isGL();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL4bc()
     */
    public boolean isGL4bc() {
	try{ return delegate.isGL4bc();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL4()
     */
    public boolean isGL4() {
	try{ return delegate.isGL4();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL3bc()
     */
    public boolean isGL3bc() {
	try{ return delegate.isGL3bc();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param unit
     * @param sampler
     * @see javax.media.opengl.GL3ES3#glBindSampler(int, int)
     */
    public void glBindSampler(int unit, int sampler) {
	try{delegate.glBindSampler(unit, sampler);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL3()
     */
    public boolean isGL3() {
	try{ return delegate.isGL3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @param flags
     * @param timeout
     * @return
     * @see javax.media.opengl.GL3ES3#glClientWaitSync(long, int, long)
     */
    public int glClientWaitSync(long sync, int flags, long timeout) {
	try{ return delegate.glClientWaitSync(sync, flags, timeout);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL2()
     */
    public boolean isGL2() {
	try{ return delegate.isGL2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param samplers
     * @see javax.media.opengl.GL3ES3#glDeleteSamplers(int, java.nio.IntBuffer)
     */
    public void glDeleteSamplers(int count, IntBuffer samplers) {
	try{delegate.glDeleteSamplers(count, samplers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES1()
     */
    public boolean isGLES1() {
	try{ return delegate.isGLES1();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES2()
     */
    public boolean isGLES2() {
	try{ return delegate.isGLES2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param samplers
     * @param samplers_offset
     * @see javax.media.opengl.GL3ES3#glDeleteSamplers(int, int[], int)
     */
    public void glDeleteSamplers(int count, int[] samplers, int samplers_offset) {
	try{delegate.glDeleteSamplers(count, samplers, samplers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES3()
     */
    public boolean isGLES3() {
	try{ return delegate.isGLES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @see javax.media.opengl.GL3ES3#glDeleteSync(long)
     */
    public void glDeleteSync(long sync) {
	try{delegate.glDeleteSync(sync);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES()
     */
    public boolean isGLES() {
	try{ return delegate.isGLES();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param condition
     * @param flags
     * @return
     * @see javax.media.opengl.GL3ES3#glFenceSync(int, int)
     */
    public long glFenceSync(int condition, int flags) {
	try{ return delegate.glFenceSync(condition, flags);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL2ES1()
     */
    public boolean isGL2ES1() {
	try{ return delegate.isGL2ES1();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param samplers
     * @see javax.media.opengl.GL3ES3#glGenSamplers(int, java.nio.IntBuffer)
     */
    public void glGenSamplers(int count, IntBuffer samplers) {
	try{delegate.glGenSamplers(count, samplers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL2ES2()
     */
    public boolean isGL2ES2() {
	try{ return delegate.isGL2ES2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL3ES3()
     */
    public boolean isGL3ES3() {
	try{ return delegate.isGL3ES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL4ES3()
     */
    public boolean isGL4ES3() {
	try{ return delegate.isGL4ES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param samplers
     * @param samplers_offset
     * @see javax.media.opengl.GL3ES3#glGenSamplers(int, int[], int)
     */
    public void glGenSamplers(int count, int[] samplers, int samplers_offset) {
	try{delegate.glGenSamplers(count, samplers, samplers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL2GL3()
     */
    public boolean isGL2GL3() {
	try{ return delegate.isGL2GL3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3ES3#glGetBufferParameteri64v(int, int, java.nio.LongBuffer)
     */
    public void glGetBufferParameteri64v(int target, int pname,
	    LongBuffer params) {
	try{delegate.glGetBufferParameteri64v(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL4core()
     */
    public boolean isGL4core() {
	try{ return delegate.isGL4core();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGL3core()
     */
    public boolean isGL3core() {
	try{ return delegate.isGL3core();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3ES3#glGetBufferParameteri64v(int, int, long[], int)
     */
    public void glGetBufferParameteri64v(int target, int pname, long[] params,
	    int params_offset) {
	try{delegate.glGetBufferParameteri64v(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLcore()
     */
    public boolean isGLcore() {
	try{ return delegate.isGLcore();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES2Compatible()
     */
    public boolean isGLES2Compatible() {
	try{ return delegate.isGLES2Compatible();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @see javax.media.opengl.GL3ES3#glGetInteger64i_v(int, int, java.nio.LongBuffer)
     */
    public void glGetInteger64i_v(int target, int index, LongBuffer data) {
	try{delegate.glGetInteger64i_v(target, index, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isGLES3Compatible()
     */
    public boolean isGLES3Compatible() {
	try{ return delegate.isGLES3Compatible();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @param data_offset
     * @see javax.media.opengl.GL3ES3#glGetInteger64i_v(int, int, long[], int)
     */
    public void glGetInteger64i_v(int target, int index, long[] data,
	    int data_offset) {
	try{delegate.glGetInteger64i_v(target, index, data, data_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param colorNumber
     * @param index
     * @param name
     * @see javax.media.opengl.GL3#glBindFragDataLocationIndexed(int, int, int, java.lang.String)
     */
    public void glBindFragDataLocationIndexed(int program, int colorNumber,
	    int index, String name) {
	try{delegate.glBindFragDataLocationIndexed(program, colorNumber, index,
		name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3ES3#glGetInteger64v(int, java.nio.LongBuffer)
     */
    public void glGetInteger64v(int pname, LongBuffer params) {
	try{delegate.glGetInteger64v(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#hasGLSL()
     */
    public boolean hasGLSL() {
	try{ return delegate.hasGLSL();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getDownstreamGL()
     */
    public GL getDownstreamGL() throws GLException {
	try{ return delegate.getDownstreamGL();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glColorP3ui(int, int)
     */
    public void glColorP3ui(int type, int color) {
	try{delegate.glColorP3ui(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3ES3#glGetInteger64v(int, long[], int)
     */
    public void glGetInteger64v(int pname, long[] params, int params_offset) {
	try{delegate.glGetInteger64v(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getRootGL()
     */
    public GL getRootGL() throws GLException {
	try{ return delegate.getRootGL();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glColorP3uiv(int, java.nio.IntBuffer)
     */
    public void glColorP3uiv(int type, IntBuffer color) {
	try{delegate.glColorP3uiv(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3ES3#glGetSamplerParameterfv(int, int, java.nio.FloatBuffer)
     */
    public void glGetSamplerParameterfv(int sampler, int pname,
	    FloatBuffer params) {
	try{delegate.glGetSamplerParameterfv(sampler, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @param color_offset
     * @see javax.media.opengl.GL3#glColorP3uiv(int, int[], int)
     */
    public void glColorP3uiv(int type, int[] color, int color_offset) {
	try{delegate.glColorP3uiv(type, color, color_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL()
     */
    public GL getGL() throws GLException {
	try{ return delegate.getGL();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL4bc()
     */
    public GL4bc getGL4bc() throws GLException {
	try{ return delegate.getGL4bc();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3ES3#glGetSamplerParameterfv(int, int, float[], int)
     */
    public void glGetSamplerParameterfv(int sampler, int pname, float[] params,
	    int params_offset) {
	try{delegate.glGetSamplerParameterfv(sampler, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glColorP4ui(int, int)
     */
    public void glColorP4ui(int type, int color) {
	try{delegate.glColorP4ui(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL4()
     */
    public GL4 getGL4() throws GLException {
	try{ return delegate.getGL4();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL3bc()
     */
    public GL3bc getGL3bc() throws GLException {
	try{ return delegate.getGL3bc();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glColorP4uiv(int, java.nio.IntBuffer)
     */
    public void glColorP4uiv(int type, IntBuffer color) {
	try{delegate.glColorP4uiv(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3ES3#glGetSamplerParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params) {
	try{delegate.glGetSamplerParameteriv(sampler, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL3()
     */
    public GL3 getGL3() throws GLException {
	try{ return delegate.getGL3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL2()
     */
    public GL2 getGL2() throws GLException {
	try{ return delegate.getGL2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @param color_offset
     * @see javax.media.opengl.GL3#glColorP4uiv(int, int[], int)
     */
    public void glColorP4uiv(int type, int[] color, int color_offset) {
	try{delegate.glColorP4uiv(type, color, color_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3ES3#glGetSamplerParameteriv(int, int, int[], int)
     */
    public void glGetSamplerParameteriv(int sampler, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetSamplerParameteriv(sampler, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGLES1()
     */
    public GLES1 getGLES1() throws GLException {
	try{ return delegate.getGLES1();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawElementsBaseVertex(int, int, int, java.nio.Buffer, int)
     */
    public void glDrawElementsBaseVertex(int mode, int count, int type,
	    Buffer indices, int basevertex) {
	try{delegate.glDrawElementsBaseVertex(mode, count, type, indices,
		basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGLES2()
     */
    public GLES2 getGLES2() throws GLException {
	try{ return delegate.getGLES2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @param pname
     * @param bufSize
     * @param length
     * @param values
     * @see javax.media.opengl.GL3ES3#glGetSynciv(long, int, int, java.nio.IntBuffer, java.nio.IntBuffer)
     */
    public void glGetSynciv(long sync, int pname, int bufSize,
	    IntBuffer length, IntBuffer values) {
	try{delegate.glGetSynciv(sync, pname, bufSize, length, values);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGLES3()
     */
    public GLES3 getGLES3() throws GLException {
	try{ return delegate.getGLES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL2ES1()
     */
    public GL2ES1 getGL2ES1() throws GLException {
	try{ return delegate.getGL2ES1();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawElementsBaseVertex(int, int, int, long, int)
     */
    public void glDrawElementsBaseVertex(int mode, int count, int type,
	    long indices_buffer_offset, int basevertex) {
	try{delegate.glDrawElementsBaseVertex(mode, count, type,
		indices_buffer_offset, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL2ES2()
     */
    public GL2ES2 getGL2ES2() throws GLException {
	try{ return delegate.getGL2ES2();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @param pname
     * @param bufSize
     * @param length
     * @param length_offset
     * @param values
     * @param values_offset
     * @see javax.media.opengl.GL3ES3#glGetSynciv(long, int, int, int[], int, int[], int)
     */
    public void glGetSynciv(long sync, int pname, int bufSize, int[] length,
	    int length_offset, int[] values, int values_offset) {
	try{delegate.glGetSynciv(sync, pname, bufSize, length, length_offset,
		values, values_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL3ES3()
     */
    public GL3ES3 getGL3ES3() throws GLException {
	try{ return delegate.getGL3ES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param instancecount
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawElementsInstancedBaseVertex(int, int, int, java.nio.Buffer, int, int)
     */
    public void glDrawElementsInstancedBaseVertex(int mode, int count,
	    int type, Buffer indices, int instancecount, int basevertex) {
	try{delegate.glDrawElementsInstancedBaseVertex(mode, count, type, indices,
		instancecount, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL4ES3()
     */
    public GL4ES3 getGL4ES3() throws GLException {
	try{ return delegate.getGL4ES3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @return
     * @see javax.media.opengl.GL3ES3#glIsSampler(int)
     */
    public boolean glIsSampler(int sampler) {
	try{ return delegate.glIsSampler(sampler);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @throws GLException
     * @see javax.media.opengl.GLBase#getGL2GL3()
     */
    public GL2GL3 getGL2GL3() throws GLException {
	try{ return delegate.getGL2GL3();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getGLProfile()
     */
    public GLProfile getGLProfile() {
	try{ return delegate.getGLProfile();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @return
     * @see javax.media.opengl.GL3ES3#glIsSync(long)
     */
    public boolean glIsSync(long sync) {
	try{ return delegate.glIsSync(sync);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @param instancecount
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawElementsInstancedBaseVertex(int, int, int, long, int, int)
     */
    public void glDrawElementsInstancedBaseVertex(int mode, int count,
	    int type, long indices_buffer_offset, int instancecount,
	    int basevertex) {
	try{delegate.glDrawElementsInstancedBaseVertex(mode, count, type,
		indices_buffer_offset, instancecount, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getContext()
     */
    public GLContext getContext() {
	try{ return delegate.getContext();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param glFunctionName
     * @return
     * @see javax.media.opengl.GLBase#isFunctionAvailable(java.lang.String)
     */
    public boolean isFunctionAvailable(String glFunctionName) {
	try{ return delegate.isFunctionAvailable(glFunctionName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3ES3#glSamplerParameterf(int, int, float)
     */
    public void glSamplerParameterf(int sampler, int pname, float param) {
	try{delegate.glSamplerParameterf(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param indices
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawRangeElementsBaseVertex(int, int, int, int, int, java.nio.Buffer, int)
     */
    public void glDrawRangeElementsBaseVertex(int mode, int start, int end,
	    int count, int type, Buffer indices, int basevertex) {
	try{delegate.glDrawRangeElementsBaseVertex(mode, start, end, count, type,
		indices, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3ES3#glSamplerParameterfv(int, int, java.nio.FloatBuffer)
     */
    public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param) {
	try{delegate.glSamplerParameterfv(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @param param_offset
     * @see javax.media.opengl.GL3ES3#glSamplerParameterfv(int, int, float[], int)
     */
    public void glSamplerParameterfv(int sampler, int pname, float[] param,
	    int param_offset) {
	try{delegate.glSamplerParameterfv(sampler, pname, param, param_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @param basevertex
     * @see javax.media.opengl.GL3#glDrawRangeElementsBaseVertex(int, int, int, int, int, long, int)
     */
    public void glDrawRangeElementsBaseVertex(int mode, int start, int end,
	    int count, int type, long indices_buffer_offset, int basevertex) {
	try{delegate.glDrawRangeElementsBaseVertex(mode, start, end, count, type,
		indices_buffer_offset, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3ES3#glSamplerParameteri(int, int, int)
     */
    public void glSamplerParameteri(int sampler, int pname, int param) {
	try{delegate.glSamplerParameteri(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param texture
     * @param level
     * @see javax.media.opengl.GL3#glFramebufferTexture(int, int, int, int)
     */
    public void glFramebufferTexture(int target, int attachment, int texture,
	    int level) {
	try{delegate.glFramebufferTexture(target, attachment, texture, level);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param glExtensionName
     * @return
     * @see javax.media.opengl.GLBase#isExtensionAvailable(java.lang.String)
     */
    public boolean isExtensionAvailable(String glExtensionName) {
	try{ return delegate.isExtensionAvailable(glExtensionName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3ES3#glSamplerParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glSamplerParameteriv(int sampler, int pname, IntBuffer param) {
	try{delegate.glSamplerParameteriv(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param name
     * @return
     * @see javax.media.opengl.GL3#glGetFragDataIndex(int, java.lang.String)
     */
    public int glGetFragDataIndex(int program, String name) {
	try{ return delegate.glGetFragDataIndex(program, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param index
     * @param val
     * @see javax.media.opengl.GL3#glGetMultisamplefv(int, int, java.nio.FloatBuffer)
     */
    public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
	try{delegate.glGetMultisamplefv(pname, index, val);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#hasBasicFBOSupport()
     */
    public boolean hasBasicFBOSupport() {
	try{ return delegate.hasBasicFBOSupport();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @param param_offset
     * @see javax.media.opengl.GL3ES3#glSamplerParameteriv(int, int, int[], int)
     */
    public void glSamplerParameteriv(int sampler, int pname, int[] param,
	    int param_offset) {
	try{delegate.glSamplerParameteriv(sampler, pname, param, param_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param index
     * @param val
     * @param val_offset
     * @see javax.media.opengl.GL3#glGetMultisamplefv(int, int, float[], int)
     */
    public void glGetMultisamplefv(int pname, int index, float[] val,
	    int val_offset) {
	try{delegate.glGetMultisamplefv(pname, index, val, val_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param divisor
     * @see javax.media.opengl.GL3ES3#glVertexAttribDivisor(int, int)
     */
    public void glVertexAttribDivisor(int index, int divisor) {
	try{delegate.glVertexAttribDivisor(index, divisor);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3#glGetQueryObjecti64v(int, int, java.nio.LongBuffer)
     */
    public void glGetQueryObjecti64v(int id, int pname, LongBuffer params) {
	try{delegate.glGetQueryObjecti64v(id, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#hasFullFBOSupport()
     */
    public boolean hasFullFBOSupport() {
	try{ return delegate.hasFullFBOSupport();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sync
     * @param flags
     * @param timeout
     * @see javax.media.opengl.GL3ES3#glWaitSync(long, int, long)
     */
    public void glWaitSync(long sync, int flags, long timeout) {
	try{delegate.glWaitSync(sync, flags, timeout);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3#glGetQueryObjecti64v(int, int, long[], int)
     */
    public void glGetQueryObjecti64v(int id, int pname, long[] params,
	    int params_offset) {
	try{delegate.glGetQueryObjecti64v(id, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getMaxRenderbufferSamples()
     */
    public int getMaxRenderbufferSamples() {
	try{ return delegate.getMaxRenderbufferSamples();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3#glGetQueryObjectui64v(int, int, java.nio.LongBuffer)
     */
    public void glGetQueryObjectui64v(int id, int pname, LongBuffer params) {
	try{delegate.glGetQueryObjectui64v(id, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isNPOTTextureAvailable()
     */
    public boolean isNPOTTextureAvailable() {
	try{ return delegate.isNPOTTextureAvailable();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3#glGetQueryObjectui64v(int, int, long[], int)
     */
    public void glGetQueryObjectui64v(int id, int pname, long[] params,
	    int params_offset) {
	try{delegate.glGetQueryObjectui64v(id, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#isTextureFormatBGRA8888Available()
     */
    public boolean isTextureFormatBGRA8888Available() {
	try{ return delegate.isTextureFormatBGRA8888Available();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param interval
     * @see javax.media.opengl.GLBase#setSwapInterval(int)
     */
    public void setSwapInterval(int interval) {
	try{delegate.setSwapInterval(interval);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3#glGetSamplerParameterIiv(int, int, java.nio.IntBuffer)
     */
    public void glGetSamplerParameterIiv(int sampler, int pname,
	    IntBuffer params) {
	try{delegate.glGetSamplerParameterIiv(sampler, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3#glGetSamplerParameterIiv(int, int, int[], int)
     */
    public void glGetSamplerParameterIiv(int sampler, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetSamplerParameterIiv(sampler, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getSwapInterval()
     */
    public int getSwapInterval() {
	try{ return delegate.getSwapInterval();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @see javax.media.opengl.GL3#glGetSamplerParameterIuiv(int, int, java.nio.IntBuffer)
     */
    public void glGetSamplerParameterIuiv(int sampler, int pname,
	    IntBuffer params) {
	try{delegate.glGetSamplerParameterIuiv(sampler, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getPlatformGLExtensions()
     */
    public Object getPlatformGLExtensions() {
	try{ return delegate.getPlatformGLExtensions();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL3#glGetSamplerParameterIuiv(int, int, int[], int)
     */
    public void glGetSamplerParameterIuiv(int sampler, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetSamplerParameterIuiv(sampler, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param drawcount
     * @param basevertex
     * @see javax.media.opengl.GL3#glMultiDrawElementsBaseVertex(int, java.nio.IntBuffer, int, com.jogamp.common.nio.PointerBuffer, int, java.nio.IntBuffer)
     */
    public void glMultiDrawElementsBaseVertex(int mode, IntBuffer count,
	    int type, PointerBuffer indices, int drawcount, IntBuffer basevertex) {
	try{delegate.glMultiDrawElementsBaseVertex(mode, count, type, indices,
		drawcount, basevertex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param extensionName
     * @return
     * @see javax.media.opengl.GLBase#getExtension(java.lang.String)
     */
    public Object getExtension(String extensionName) {
	try{ return delegate.getExtension(extensionName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP1ui(int, int, int)
     */
    public void glMultiTexCoordP1ui(int texture, int type, int coords) {
	try{delegate.glMultiTexCoordP1ui(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP1uiv(int, int, java.nio.IntBuffer)
     */
    public void glMultiTexCoordP1uiv(int texture, int type, IntBuffer coords) {
	try{delegate.glMultiTexCoordP1uiv(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glMultiTexCoordP1uiv(int, int, int[], int)
     */
    public void glMultiTexCoordP1uiv(int texture, int type, int[] coords,
	    int coords_offset) {
	try{delegate.glMultiTexCoordP1uiv(texture, type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @return
     * @see javax.media.opengl.GLBase#glGetBoundBuffer(int)
     */
    public int glGetBoundBuffer(int target) {
	try{ return delegate.glGetBoundBuffer(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP2ui(int, int, int)
     */
    public void glMultiTexCoordP2ui(int texture, int type, int coords) {
	try{delegate.glMultiTexCoordP2ui(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @return
     * @see javax.media.opengl.GLBase#glGetBufferSize(int)
     */
    public long glGetBufferSize(int buffer) {
	try{ return delegate.glGetBufferSize(buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP2uiv(int, int, java.nio.IntBuffer)
     */
    public void glMultiTexCoordP2uiv(int texture, int type, IntBuffer coords) {
	try{delegate.glMultiTexCoordP2uiv(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#glIsVBOArrayEnabled()
     */
    public boolean glIsVBOArrayEnabled() {
	try{ return delegate.glIsVBOArrayEnabled();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#glIsVBOElementArrayEnabled()
     */
    public boolean glIsVBOElementArrayEnabled() {
	try{ return delegate.glIsVBOElementArrayEnabled();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glMultiTexCoordP2uiv(int, int, int[], int)
     */
    public void glMultiTexCoordP2uiv(int texture, int type, int[] coords,
	    int coords_offset) {
	try{delegate.glMultiTexCoordP2uiv(texture, type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @return
     * @see javax.media.opengl.GLBase#getBoundFramebuffer(int)
     */
    public int getBoundFramebuffer(int target) {
	try{ return delegate.getBoundFramebuffer(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getDefaultDrawFramebuffer()
     */
    public int getDefaultDrawFramebuffer() {
	try{ return delegate.getDefaultDrawFramebuffer();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP3ui(int, int, int)
     */
    public void glMultiTexCoordP3ui(int texture, int type, int coords) {
	try{delegate.glMultiTexCoordP3ui(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getDefaultReadFramebuffer()
     */
    public int getDefaultReadFramebuffer() {
	try{ return delegate.getDefaultReadFramebuffer();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP3uiv(int, int, java.nio.IntBuffer)
     */
    public void glMultiTexCoordP3uiv(int texture, int type, IntBuffer coords) {
	try{delegate.glMultiTexCoordP3uiv(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GLBase#getDefaultReadBuffer()
     */
    public int getDefaultReadBuffer() {
	try{ return delegate.getDefaultReadBuffer();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glMultiTexCoordP3uiv(int, int, int[], int)
     */
    public void glMultiTexCoordP3uiv(int texture, int type, int[] coords,
	    int coords_offset) {
	try{delegate.glMultiTexCoordP3uiv(texture, type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP4ui(int, int, int)
     */
    public void glMultiTexCoordP4ui(int texture, int type, int coords) {
	try{delegate.glMultiTexCoordP4ui(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glMultiTexCoordP4uiv(int, int, java.nio.IntBuffer)
     */
    public void glMultiTexCoordP4uiv(int texture, int type, IntBuffer coords) {
	try{delegate.glMultiTexCoordP4uiv(texture, type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glMultiTexCoordP4uiv(int, int, int[], int)
     */
    public void glMultiTexCoordP4uiv(int texture, int type, int[] coords,
	    int coords_offset) {
	try{delegate.glMultiTexCoordP4uiv(texture, type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glNormalP3ui(int, int)
     */
    public void glNormalP3ui(int type, int coords) {
	try{delegate.glNormalP3ui(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glNormalP3uiv(int, java.nio.IntBuffer)
     */
    public void glNormalP3uiv(int type, IntBuffer coords) {
	try{delegate.glNormalP3uiv(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glNormalP3uiv(int, int[], int)
     */
    public void glNormalP3uiv(int type, int[] coords, int coords_offset) {
	try{delegate.glNormalP3uiv(type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL3#glProvokingVertex(int)
     */
    public void glProvokingVertex(int mode) {
	try{delegate.glProvokingVertex(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param target
     * @see javax.media.opengl.GL3#glQueryCounter(int, int)
     */
    public void glQueryCounter(int id, int target) {
	try{delegate.glQueryCounter(id, target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param mask
     * @see javax.media.opengl.GL3#glSampleMaski(int, int)
     */
    public void glSampleMaski(int index, int mask) {
	try{delegate.glSampleMaski(index, mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3#glSamplerParameterIiv(int, int, java.nio.IntBuffer)
     */
    public void glSamplerParameterIiv(int sampler, int pname, IntBuffer param) {
	try{delegate.glSamplerParameterIiv(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @param param_offset
     * @see javax.media.opengl.GL3#glSamplerParameterIiv(int, int, int[], int)
     */
    public void glSamplerParameterIiv(int sampler, int pname, int[] param,
	    int param_offset) {
	try{delegate.glSamplerParameterIiv(sampler, pname, param, param_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @see javax.media.opengl.GL3#glSamplerParameterIuiv(int, int, java.nio.IntBuffer)
     */
    public void glSamplerParameterIuiv(int sampler, int pname, IntBuffer param) {
	try{delegate.glSamplerParameterIuiv(sampler, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sampler
     * @param pname
     * @param param
     * @param param_offset
     * @see javax.media.opengl.GL3#glSamplerParameterIuiv(int, int, int[], int)
     */
    public void glSamplerParameterIuiv(int sampler, int pname, int[] param,
	    int param_offset) {
	try{delegate.glSamplerParameterIuiv(sampler, pname, param, param_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glSecondaryColorP3ui(int, int)
     */
    public void glSecondaryColorP3ui(int type, int color) {
	try{delegate.glSecondaryColorP3ui(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @see javax.media.opengl.GL3#glSecondaryColorP3uiv(int, java.nio.IntBuffer)
     */
    public void glSecondaryColorP3uiv(int type, IntBuffer color) {
	try{delegate.glSecondaryColorP3uiv(type, color);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param color
     * @param color_offset
     * @see javax.media.opengl.GL3#glSecondaryColorP3uiv(int, int[], int)
     */
    public void glSecondaryColorP3uiv(int type, int[] color, int color_offset) {
	try{delegate.glSecondaryColorP3uiv(type, color, color_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP1ui(int, int)
     */
    public void glTexCoordP1ui(int type, int coords) {
	try{delegate.glTexCoordP1ui(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP1uiv(int, java.nio.IntBuffer)
     */
    public void glTexCoordP1uiv(int type, IntBuffer coords) {
	try{delegate.glTexCoordP1uiv(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glTexCoordP1uiv(int, int[], int)
     */
    public void glTexCoordP1uiv(int type, int[] coords, int coords_offset) {
	try{delegate.glTexCoordP1uiv(type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP2ui(int, int)
     */
    public void glTexCoordP2ui(int type, int coords) {
	try{delegate.glTexCoordP2ui(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP2uiv(int, java.nio.IntBuffer)
     */
    public void glTexCoordP2uiv(int type, IntBuffer coords) {
	try{delegate.glTexCoordP2uiv(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glTexCoordP2uiv(int, int[], int)
     */
    public void glTexCoordP2uiv(int type, int[] coords, int coords_offset) {
	try{delegate.glTexCoordP2uiv(type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP3ui(int, int)
     */
    public void glTexCoordP3ui(int type, int coords) {
	try{delegate.glTexCoordP3ui(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP3uiv(int, java.nio.IntBuffer)
     */
    public void glTexCoordP3uiv(int type, IntBuffer coords) {
	try{delegate.glTexCoordP3uiv(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glTexCoordP3uiv(int, int[], int)
     */
    public void glTexCoordP3uiv(int type, int[] coords, int coords_offset) {
	try{delegate.glTexCoordP3uiv(type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP4ui(int, int)
     */
    public void glTexCoordP4ui(int type, int coords) {
	try{delegate.glTexCoordP4ui(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @see javax.media.opengl.GL3#glTexCoordP4uiv(int, java.nio.IntBuffer)
     */
    public void glTexCoordP4uiv(int type, IntBuffer coords) {
	try{delegate.glTexCoordP4uiv(type, coords);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param coords
     * @param coords_offset
     * @see javax.media.opengl.GL3#glTexCoordP4uiv(int, int[], int)
     */
    public void glTexCoordP4uiv(int type, int[] coords, int coords_offset) {
	try{delegate.glTexCoordP4uiv(type, coords, coords_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param samples
     * @param internalformat
     * @param width
     * @param height
     * @param fixedsamplelocations
     * @see javax.media.opengl.GL3#glTexImage2DMultisample(int, int, int, int, int, boolean)
     */
    public void glTexImage2DMultisample(int target, int samples,
	    int internalformat, int width, int height,
	    boolean fixedsamplelocations) {
	try{delegate.glTexImage2DMultisample(target, samples, internalformat,
		width, height, fixedsamplelocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param samples
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param fixedsamplelocations
     * @see javax.media.opengl.GL3#glTexImage3DMultisample(int, int, int, int, int, int, boolean)
     */
    public void glTexImage3DMultisample(int target, int samples,
	    int internalformat, int width, int height, int depth,
	    boolean fixedsamplelocations) {
	try{delegate.glTexImage3DMultisample(target, samples, internalformat,
		width, height, depth, fixedsamplelocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP1ui(int, int, boolean, int)
     */
    public void glVertexAttribP1ui(int index, int type, boolean normalized,
	    int value) {
	try{delegate.glVertexAttribP1ui(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP1uiv(int, int, boolean, java.nio.IntBuffer)
     */
    public void glVertexAttribP1uiv(int index, int type, boolean normalized,
	    IntBuffer value) {
	try{delegate.glVertexAttribP1uiv(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param primitiveMode
     * @see javax.media.opengl.GL2ES3#glBeginTransformFeedback(int)
     */
    public void glBeginTransformFeedback(int primitiveMode) {
	try{delegate.glBeginTransformFeedback(primitiveMode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexAttribP1uiv(int, int, boolean, int[], int)
     */
    public void glVertexAttribP1uiv(int index, int type, boolean normalized,
	    int[] value, int value_offset) {
	try{delegate.glVertexAttribP1uiv(index, type, normalized, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param buffer
     * @see javax.media.opengl.GL2ES3#glBindBufferBase(int, int, int)
     */
    public void glBindBufferBase(int target, int index, int buffer) {
	try{delegate.glBindBufferBase(target, index, buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param buffer
     * @param offset
     * @param size
     * @see javax.media.opengl.GL2ES3#glBindBufferRange(int, int, int, long, long)
     */
    public void glBindBufferRange(int target, int index, int buffer,
	    long offset, long size) {
	try{delegate.glBindBufferRange(target, index, buffer, offset, size);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP2ui(int, int, boolean, int)
     */
    public void glVertexAttribP2ui(int index, int type, boolean normalized,
	    int value) {
	try{delegate.glVertexAttribP2ui(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param array
     * @see javax.media.opengl.GL2ES3#glBindVertexArray(int)
     */
    public void glBindVertexArray(int array) {
	try{delegate.glBindVertexArray(array);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP2uiv(int, int, boolean, java.nio.IntBuffer)
     */
    public void glVertexAttribP2uiv(int index, int type, boolean normalized,
	    IntBuffer value) {
	try{delegate.glVertexAttribP2uiv(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param mode
     * @see javax.media.opengl.GL2GL3#glBeginConditionalRender(int, int)
     */
    public void glBeginConditionalRender(int id, int mode) {
	try{delegate.glBeginConditionalRender(id, mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param srcX0
     * @param srcY0
     * @param srcX1
     * @param srcY1
     * @param dstX0
     * @param dstY0
     * @param dstX1
     * @param dstY1
     * @param mask
     * @param filter
     * @see javax.media.opengl.GL2ES3#glBlitFramebuffer(int, int, int, int, int, int, int, int, int, int)
     */
    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1,
	    int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
	try{delegate.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0,
		dstX1, dstY1, mask, filter);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param color
     * @param name
     * @see javax.media.opengl.GL2GL3#glBindFragDataLocation(int, int, java.lang.String)
     */
    public void glBindFragDataLocation(int program, int color, String name) {
	try{delegate.glBindFragDataLocation(program, color, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexAttribP2uiv(int, int, boolean, int[], int)
     */
    public void glVertexAttribP2uiv(int index, int type, boolean normalized,
	    int[] value, int value_offset) {
	try{delegate.glVertexAttribP2uiv(index, type, normalized, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param index
     * @param address
     * @param length
     * @see javax.media.opengl.GL2GL3#glBufferAddressRangeNV(int, int, long, long)
     */
    public void glBufferAddressRangeNV(int pname, int index, long address,
	    long length) {
	try{delegate.glBufferAddressRangeNV(pname, index, address, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param depth
     * @param stencil
     * @see javax.media.opengl.GL2ES3#glClearBufferfi(int, int, float, int)
     */
    public void glClearBufferfi(int buffer, int drawbuffer, float depth,
	    int stencil) {
	try{delegate.glClearBufferfi(buffer, drawbuffer, depth, stencil);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP3ui(int, int, boolean, int)
     */
    public void glVertexAttribP3ui(int index, int type, boolean normalized,
	    int value) {
	try{delegate.glVertexAttribP3ui(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param clamp
     * @see javax.media.opengl.GL2GL3#glClampColor(int, int)
     */
    public void glClampColor(int target, int clamp) {
	try{delegate.glClampColor(target, clamp);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @see javax.media.opengl.GL2ES3#glClearBufferfv(int, int, java.nio.FloatBuffer)
     */
    public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
	try{delegate.glClearBufferfv(buffer, drawbuffer, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP3uiv(int, int, boolean, java.nio.IntBuffer)
     */
    public void glVertexAttribP3uiv(int index, int type, boolean normalized,
	    IntBuffer value) {
	try{delegate.glVertexAttribP3uiv(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param internalformat
     * @param format
     * @param type
     * @param data
     * @see javax.media.opengl.GL2GL3#glClearNamedBufferDataEXT(int, int, int, int, java.nio.Buffer)
     */
    public void glClearNamedBufferDataEXT(int buffer, int internalformat,
	    int format, int type, Buffer data) {
	try{delegate.glClearNamedBufferDataEXT(buffer, internalformat, format,
		type, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param shader
     * @see javax.media.opengl.GL2ES2#glAttachShader(int, int)
     */
    public void glAttachShader(int program, int shader) {
	try{delegate.glAttachShader(program, shader);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glClearBufferfv(int, int, float[], int)
     */
    public void glClearBufferfv(int buffer, int drawbuffer, float[] value,
	    int value_offset) {
	try{delegate.glClearBufferfv(buffer, drawbuffer, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexAttribP3uiv(int, int, boolean, int[], int)
     */
    public void glVertexAttribP3uiv(int index, int type, boolean normalized,
	    int[] value, int value_offset) {
	try{delegate.glVertexAttribP3uiv(index, type, normalized, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param id
     * @see javax.media.opengl.GL2ES2#glBeginQuery(int, int)
     */
    public void glBeginQuery(int target, int id) {
	try{delegate.glBeginQuery(target, id);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param internalformat
     * @param format
     * @param type
     * @param offset
     * @param size
     * @param data
     * @see javax.media.opengl.GL2GL3#glClearNamedBufferSubDataEXT(int, int, int, int, long, long, java.nio.Buffer)
     */
    public void glClearNamedBufferSubDataEXT(int buffer, int internalformat,
	    int format, int type, long offset, long size, Buffer data) {
	try{delegate.glClearNamedBufferSubDataEXT(buffer, internalformat, format,
		type, offset, size, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @see javax.media.opengl.GL2ES3#glClearBufferiv(int, int, java.nio.IntBuffer)
     */
    public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
	try{delegate.glClearBufferiv(buffer, drawbuffer, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param name
     * @see javax.media.opengl.GL2ES2#glBindAttribLocation(int, int, java.lang.String)
     */
    public void glBindAttribLocation(int program, int index, String name) {
	try{delegate.glBindAttribLocation(program, index, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP4ui(int, int, boolean, int)
     */
    public void glVertexAttribP4ui(int index, int type, boolean normalized,
	    int value) {
	try{delegate.glVertexAttribP4ui(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param size
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glColorFormatNV(int, int, int)
     */
    public void glColorFormatNV(int size, int type, int stride) {
	try{delegate.glColorFormatNV(size, type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glClearBufferiv(int, int, int[], int)
     */
    public void glClearBufferiv(int buffer, int drawbuffer, int[] value,
	    int value_offset) {
	try{delegate.glClearBufferiv(buffer, drawbuffer, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @see javax.media.opengl.GL2ES2#glBlendColor(float, float, float, float)
     */
    public void glBlendColor(float red, float green, float blue, float alpha) {
	try{delegate.glBlendColor(red, green, blue, alpha);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @see javax.media.opengl.GL3#glVertexAttribP4uiv(int, int, boolean, java.nio.IntBuffer)
     */
    public void glVertexAttribP4uiv(int index, int type, boolean normalized,
	    IntBuffer value) {
	try{delegate.glVertexAttribP4uiv(index, type, normalized, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param r
     * @param g
     * @param b
     * @param a
     * @see javax.media.opengl.GL2GL3#glColorMaski(int, boolean, boolean, boolean, boolean)
     */
    public void glColorMaski(int index, boolean r, boolean g, boolean b,
	    boolean a) {
	try{delegate.glColorMaski(index, r, g, b, a);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @see javax.media.opengl.GL2ES3#glClearBufferuiv(int, int, java.nio.IntBuffer)
     */
    public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value) {
	try{delegate.glClearBufferuiv(buffer, drawbuffer, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @see javax.media.opengl.GL2ES2#glCompileShader(int)
     */
    public void glCompileShader(int shader) {
	try{delegate.glCompileShader(shader);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param count
     * @param path
     * @param length
     * @see javax.media.opengl.GL2GL3#glCompileShaderIncludeARB(int, int, java.lang.String[], java.nio.IntBuffer)
     */
    public void glCompileShaderIncludeARB(int shader, int count, String[] path,
	    IntBuffer length) {
	try{delegate.glCompileShaderIncludeARB(shader, count, path, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param type
     * @param normalized
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexAttribP4uiv(int, int, boolean, int[], int)
     */
    public void glVertexAttribP4uiv(int index, int type, boolean normalized,
	    int[] value, int value_offset) {
	try{delegate.glVertexAttribP4uiv(index, type, normalized, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param border
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL2ES2#glCompressedTexImage3D(int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexImage3D(int target, int level,
	    int internalformat, int width, int height, int depth, int border,
	    int imageSize, Buffer data) {
	try{delegate.glCompressedTexImage3D(target, level, internalformat, width,
		height, depth, border, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param drawbuffer
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glClearBufferuiv(int, int, int[], int)
     */
    public void glClearBufferuiv(int buffer, int drawbuffer, int[] value,
	    int value_offset) {
	try{delegate.glClearBufferuiv(buffer, drawbuffer, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param count
     * @param path
     * @param length
     * @param length_offset
     * @see javax.media.opengl.GL2GL3#glCompileShaderIncludeARB(int, int, java.lang.String[], int[], int)
     */
    public void glCompileShaderIncludeARB(int shader, int count, String[] path,
	    int[] length, int length_offset) {
	try{delegate.glCompileShaderIncludeARB(shader, count, path, length,
		length_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP2ui(int, int)
     */
    public void glVertexP2ui(int type, int value) {
	try{delegate.glVertexP2ui(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param readTarget
     * @param writeTarget
     * @param readOffset
     * @param writeOffset
     * @param size
     * @see javax.media.opengl.GL2ES3#glCopyBufferSubData(int, int, long, long, long)
     */
    public void glCopyBufferSubData(int readTarget, int writeTarget,
	    long readOffset, long writeOffset, long size) {
	try{delegate.glCopyBufferSubData(readTarget, writeTarget, readOffset,
		writeOffset, size);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param border
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL2ES2#glCompressedTexImage3D(int, int, int, int, int, int, int, int, long)
     */
    public void glCompressedTexImage3D(int target, int level,
	    int internalformat, int width, int height, int depth, int border,
	    int imageSize, long data_buffer_offset) {
	try{delegate.glCompressedTexImage3D(target, level, internalformat, width,
		height, depth, border, imageSize, data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP2uiv(int, java.nio.IntBuffer)
     */
    public void glVertexP2uiv(int type, IntBuffer value) {
	try{delegate.glVertexP2uiv(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param border
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL2GL3#glCompressedTexImage1D(int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexImage1D(int target, int level,
	    int internalformat, int width, int border, int imageSize,
	    Buffer data) {
	try{delegate.glCompressedTexImage1D(target, level, internalformat, width,
		border, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param arrays
     * @see javax.media.opengl.GL2ES3#glDeleteVertexArrays(int, java.nio.IntBuffer)
     */
    public void glDeleteVertexArrays(int n, IntBuffer arrays) {
	try{delegate.glDeleteVertexArrays(n, arrays);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexP2uiv(int, int[], int)
     */
    public void glVertexP2uiv(int type, int[] value, int value_offset) {
	try{delegate.glVertexP2uiv(type, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param border
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL2GL3#glCompressedTexImage1D(int, int, int, int, int, int, long)
     */
    public void glCompressedTexImage1D(int target, int level,
	    int internalformat, int width, int border, int imageSize,
	    long data_buffer_offset) {
	try{delegate.glCompressedTexImage1D(target, level, internalformat, width,
		border, imageSize, data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param width
     * @param height
     * @param depth
     * @param format
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL2ES2#glCompressedTexSubImage3D(int, int, int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexSubImage3D(int target, int level, int xoffset,
	    int yoffset, int zoffset, int width, int height, int depth,
	    int format, int imageSize, Buffer data) {
	try{delegate.glCompressedTexSubImage3D(target, level, xoffset, yoffset,
		zoffset, width, height, depth, format, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP3ui(int, int)
     */
    public void glVertexP3ui(int type, int value) {
	try{delegate.glVertexP3ui(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param arrays
     * @param arrays_offset
     * @see javax.media.opengl.GL2ES3#glDeleteVertexArrays(int, int[], int)
     */
    public void glDeleteVertexArrays(int n, int[] arrays, int arrays_offset) {
	try{delegate.glDeleteVertexArrays(n, arrays, arrays_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP3uiv(int, java.nio.IntBuffer)
     */
    public void glVertexP3uiv(int type, IntBuffer value) {
	try{delegate.glVertexP3uiv(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param width
     * @param format
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL2GL3#glCompressedTexSubImage1D(int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexSubImage1D(int target, int level, int xoffset,
	    int width, int format, int imageSize, Buffer data) {
	try{delegate.glCompressedTexSubImage1D(target, level, xoffset, width,
		format, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param first
     * @param count
     * @param instancecount
     * @see javax.media.opengl.GL2ES3#glDrawArraysInstanced(int, int, int, int)
     */
    public void glDrawArraysInstanced(int mode, int first, int count,
	    int instancecount) {
	try{delegate.glDrawArraysInstanced(mode, first, count, instancecount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param width
     * @param height
     * @param depth
     * @param format
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL2ES2#glCompressedTexSubImage3D(int, int, int, int, int, int, int, int, int, int, long)
     */
    public void glCompressedTexSubImage3D(int target, int level, int xoffset,
	    int yoffset, int zoffset, int width, int height, int depth,
	    int format, int imageSize, long data_buffer_offset) {
	try{delegate.glCompressedTexSubImage3D(target, level, xoffset, yoffset,
		zoffset, width, height, depth, format, imageSize,
		data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexP3uiv(int, int[], int)
     */
    public void glVertexP3uiv(int type, int[] value, int value_offset) {
	try{delegate.glVertexP3uiv(type, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param bufs
     * @see javax.media.opengl.GL2ES3#glDrawBuffers(int, java.nio.IntBuffer)
     */
    public void glDrawBuffers(int n, IntBuffer bufs) {
	try{delegate.glDrawBuffers(n, bufs);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param width
     * @param format
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL2GL3#glCompressedTexSubImage1D(int, int, int, int, int, int, long)
     */
    public void glCompressedTexSubImage1D(int target, int level, int xoffset,
	    int width, int format, int imageSize, long data_buffer_offset) {
	try{delegate.glCompressedTexSubImage1D(target, level, xoffset, width,
		format, imageSize, data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP4ui(int, int)
     */
    public void glVertexP4ui(int type, int value) {
	try{delegate.glVertexP4ui(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param x
     * @param y
     * @param width
     * @param height
     * @see javax.media.opengl.GL2ES2#glCopyTexSubImage3D(int, int, int, int, int, int, int, int, int)
     */
    public void glCopyTexSubImage3D(int target, int level, int xoffset,
	    int yoffset, int zoffset, int x, int y, int width, int height) {
	try{delegate.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset,
		x, y, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param bufs
     * @param bufs_offset
     * @see javax.media.opengl.GL2ES3#glDrawBuffers(int, int[], int)
     */
    public void glDrawBuffers(int n, int[] bufs, int bufs_offset) {
	try{delegate.glDrawBuffers(n, bufs, bufs_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param x
     * @param y
     * @param width
     * @param border
     * @see javax.media.opengl.GL2GL3#glCopyTexImage1D(int, int, int, int, int, int, int)
     */
    public void glCopyTexImage1D(int target, int level, int internalformat,
	    int x, int y, int width, int border) {
	try{delegate.glCopyTexImage1D(target, level, internalformat, x, y, width,
		border);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @see javax.media.opengl.GL3#glVertexP4uiv(int, java.nio.IntBuffer)
     */
    public void glVertexP4uiv(int type, IntBuffer value) {
	try{delegate.glVertexP4uiv(type, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param instancecount
     * @see javax.media.opengl.GL2ES3#glDrawElementsInstanced(int, int, int, java.nio.Buffer, int)
     */
    public void glDrawElementsInstanced(int mode, int count, int type,
	    Buffer indices, int instancecount) {
	try{delegate.glDrawElementsInstanced(mode, count, type, indices,
		instancecount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param x
     * @param y
     * @param width
     * @see javax.media.opengl.GL2GL3#glCopyTexSubImage1D(int, int, int, int, int, int)
     */
    public void glCopyTexSubImage1D(int target, int level, int xoffset, int x,
	    int y, int width) {
	try{delegate.glCopyTexSubImage1D(target, level, xoffset, x, y, width);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL3#glVertexP4uiv(int, int[], int)
     */
    public void glVertexP4uiv(int type, int[] value, int value_offset) {
	try{delegate.glVertexP4uiv(type, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GL2ES2#glCreateProgram()
     */
    public int glCreateProgram() {
	try{ return delegate.glCreateProgram();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @return
     * @see javax.media.opengl.GL2ES2#glCreateShader(int)
     */
    public int glCreateShader(int type) {
	try{ return delegate.glCreateShader(type);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param context
     * @param event
     * @param flags
     * @return
     * @see javax.media.opengl.GL2GL3#glCreateSyncFromCLeventARB(long, long, int)
     */
    public long glCreateSyncFromCLeventARB(long context, long event, int flags) {
	try{ return delegate.glCreateSyncFromCLeventARB(context, event, flags);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @param instancecount
     * @see javax.media.opengl.GL2ES3#glDrawElementsInstanced(int, int, int, long, int)
     */
    public void glDrawElementsInstanced(int mode, int count, int type,
	    long indices_buffer_offset, int instancecount) {
	try{delegate.glDrawElementsInstanced(mode, count, type,
		indices_buffer_offset, instancecount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param source
     * @param type
     * @param severity
     * @param count
     * @param ids
     * @param enabled
     * @see javax.media.opengl.GL2ES2#glDebugMessageControl(int, int, int, int, java.nio.IntBuffer, boolean)
     */
    public void glDebugMessageControl(int source, int type, int severity,
	    int count, IntBuffer ids, boolean enabled) {
	try{delegate.glDebugMessageControl(source, type, severity, count, ids,
		enabled);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param category
     * @param severity
     * @param count
     * @param ids
     * @param enabled
     * @see javax.media.opengl.GL2GL3#glDebugMessageEnableAMD(int, int, int, java.nio.IntBuffer, boolean)
     */
    public void glDebugMessageEnableAMD(int category, int severity, int count,
	    IntBuffer ids, boolean enabled) {
	try{delegate.glDebugMessageEnableAMD(category, severity, count, ids,
		enabled);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param indices
     * @see javax.media.opengl.GL2ES3#glDrawRangeElements(int, int, int, int, int, java.nio.Buffer)
     */
    public void glDrawRangeElements(int mode, int start, int end, int count,
	    int type, Buffer indices) {
	try{delegate.glDrawRangeElements(mode, start, end, count, type, indices);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param source
     * @param type
     * @param severity
     * @param count
     * @param ids
     * @param ids_offset
     * @param enabled
     * @see javax.media.opengl.GL2ES2#glDebugMessageControl(int, int, int, int, int[], int, boolean)
     */
    public void glDebugMessageControl(int source, int type, int severity,
	    int count, int[] ids, int ids_offset, boolean enabled) {
	try{delegate.glDebugMessageControl(source, type, severity, count, ids,
		ids_offset, enabled);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param category
     * @param severity
     * @param count
     * @param ids
     * @param ids_offset
     * @param enabled
     * @see javax.media.opengl.GL2GL3#glDebugMessageEnableAMD(int, int, int, int[], int, boolean)
     */
    public void glDebugMessageEnableAMD(int category, int severity, int count,
	    int[] ids, int ids_offset, boolean enabled) {
	try{delegate.glDebugMessageEnableAMD(category, severity, count, ids,
		ids_offset, enabled);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param start
     * @param end
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @see javax.media.opengl.GL2ES3#glDrawRangeElements(int, int, int, int, int, long)
     */
    public void glDrawRangeElements(int mode, int start, int end, int count,
	    int type, long indices_buffer_offset) {
	try{delegate.glDrawRangeElements(mode, start, end, count, type,
		indices_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param category
     * @param severity
     * @param id
     * @param length
     * @param buf
     * @see javax.media.opengl.GL2GL3#glDebugMessageInsertAMD(int, int, int, int, java.lang.String)
     */
    public void glDebugMessageInsertAMD(int category, int severity, int id,
	    int length, String buf) {
	try{delegate.glDebugMessageInsertAMD(category, severity, id, length, buf);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param source
     * @param type
     * @param id
     * @param severity
     * @param length
     * @param buf
     * @see javax.media.opengl.GL2ES2#glDebugMessageInsert(int, int, int, int, int, java.lang.String)
     */
    public void glDebugMessageInsert(int source, int type, int id,
	    int severity, int length, String buf) {
	try{delegate.glDebugMessageInsert(source, type, id, severity, length, buf);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL2ES3#glEndTransformFeedback()
     */
    public void glEndTransformFeedback() {
	try{delegate.glEndTransformFeedback();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @see javax.media.opengl.GL2GL3#glDeleteNamedStringARB(int, java.lang.String)
     */
    public void glDeleteNamedStringARB(int namelen, String name) {
	try{delegate.glDeleteNamedStringARB(namelen, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param texture
     * @param level
     * @param layer
     * @see javax.media.opengl.GL2ES3#glFramebufferTextureLayer(int, int, int, int, int)
     */
    public void glFramebufferTextureLayer(int target, int attachment,
	    int texture, int level, int layer) {
	try{delegate.glFramebufferTextureLayer(target, attachment, texture, level,
		layer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @see javax.media.opengl.GL2ES2#glDeleteProgram(int)
     */
    public void glDeleteProgram(int program) {
	try{delegate.glDeleteProgram(program);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param cap
     * @see javax.media.opengl.GL2GL3#glDisableClientState(int)
     */
    public void glDisableClientState(int cap) {
	try{delegate.glDisableClientState(cap);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param ids
     * @see javax.media.opengl.GL2ES2#glDeleteQueries(int, java.nio.IntBuffer)
     */
    public void glDeleteQueries(int n, IntBuffer ids) {
	try{delegate.glDeleteQueries(n, ids);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @see javax.media.opengl.GL2GL3#glDisablei(int, int)
     */
    public void glDisablei(int target, int index) {
	try{delegate.glDisablei(target, index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param arrays
     * @see javax.media.opengl.GL2ES3#glGenVertexArrays(int, java.nio.IntBuffer)
     */
    public void glGenVertexArrays(int n, IntBuffer arrays) {
	try{delegate.glGenVertexArrays(n, arrays);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL2GL3#glDrawBuffer(int)
     */
    public void glDrawBuffer(int mode) {
	try{delegate.glDrawBuffer(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param ids
     * @param ids_offset
     * @see javax.media.opengl.GL2ES2#glDeleteQueries(int, int[], int)
     */
    public void glDeleteQueries(int n, int[] ids, int ids_offset) {
	try{delegate.glDeleteQueries(n, ids, ids_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param arrays
     * @param arrays_offset
     * @see javax.media.opengl.GL2ES3#glGenVertexArrays(int, int[], int)
     */
    public void glGenVertexArrays(int n, int[] arrays, int arrays_offset) {
	try{delegate.glGenVertexArrays(n, arrays, arrays_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param stride
     * @see javax.media.opengl.GL2GL3#glEdgeFlagFormatNV(int)
     */
    public void glEdgeFlagFormatNV(int stride) {
	try{delegate.glEdgeFlagFormatNV(stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param cap
     * @see javax.media.opengl.GL2GL3#glEnableClientState(int)
     */
    public void glEnableClientState(int cap) {
	try{delegate.glEnableClientState(cap);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @see javax.media.opengl.GL2ES2#glDeleteShader(int)
     */
    public void glDeleteShader(int shader) {
	try{delegate.glDeleteShader(shader);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockIndex
     * @param bufSize
     * @param length
     * @param uniformBlockName
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformBlockName(int, int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex,
	    int bufSize, IntBuffer length, ByteBuffer uniformBlockName) {
	try{delegate.glGetActiveUniformBlockName(program, uniformBlockIndex,
		bufSize, length, uniformBlockName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param shader
     * @see javax.media.opengl.GL2ES2#glDetachShader(int, int)
     */
    public void glDetachShader(int program, int shader) {
	try{delegate.glDetachShader(program, shader);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @see javax.media.opengl.GL2GL3#glEnablei(int, int)
     */
    public void glEnablei(int target, int index) {
	try{delegate.glEnablei(target, index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL2GL3#glEndConditionalRender()
     */
    public void glEndConditionalRender() {
	try{delegate.glEndConditionalRender();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @see javax.media.opengl.GL2ES2#glDisableVertexAttribArray(int)
     */
    public void glDisableVertexAttribArray(int index) {
	try{delegate.glDisableVertexAttribArray(index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockIndex
     * @param bufSize
     * @param length
     * @param length_offset
     * @param uniformBlockName
     * @param uniformBlockName_offset
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformBlockName(int, int, int, int[], int, byte[], int)
     */
    public void glGetActiveUniformBlockName(int program, int uniformBlockIndex,
	    int bufSize, int[] length, int length_offset,
	    byte[] uniformBlockName, int uniformBlockName_offset) {
	try{delegate.glGetActiveUniformBlockName(program, uniformBlockIndex,
		bufSize, length, length_offset, uniformBlockName,
		uniformBlockName_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glFogCoordFormatNV(int, int)
     */
    public void glFogCoordFormatNV(int type, int stride) {
	try{delegate.glFogCoordFormatNV(type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @see javax.media.opengl.GL2ES2#glEnableVertexAttribArray(int)
     */
    public void glEnableVertexAttribArray(int index) {
	try{delegate.glEnableVertexAttribArray(index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param textarget
     * @param texture
     * @param level
     * @see javax.media.opengl.GL2GL3#glFramebufferTexture1D(int, int, int, int, int)
     */
    public void glFramebufferTexture1D(int target, int attachment,
	    int textarget, int texture, int level) {
	try{delegate.glFramebufferTexture1D(target, attachment, textarget, texture,
		level);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @see javax.media.opengl.GL2ES2#glEndQuery(int)
     */
    public void glEndQuery(int target) {
	try{delegate.glEndQuery(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockIndex
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformBlockiv(int, int, int, java.nio.IntBuffer)
     */
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex,
	    int pname, IntBuffer params) {
	try{delegate.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname,
		params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param textarget
     * @param texture
     * @param level
     * @param zoffset
     * @see javax.media.opengl.GL2ES2#glFramebufferTexture3D(int, int, int, int, int, int)
     */
    public void glFramebufferTexture3D(int target, int attachment,
	    int textarget, int texture, int level, int zoffset) {
	try{delegate.glFramebufferTexture3D(target, attachment, textarget, texture,
		level, zoffset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param texture
     * @param level
     * @see javax.media.opengl.GL2GL3#glFramebufferTextureARB(int, int, int, int)
     */
    public void glFramebufferTextureARB(int target, int attachment,
	    int texture, int level) {
	try{delegate.glFramebufferTextureARB(target, attachment, texture, level);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param texture
     * @param level
     * @param face
     * @see javax.media.opengl.GL2GL3#glFramebufferTextureFaceARB(int, int, int, int, int)
     */
    public void glFramebufferTextureFaceARB(int target, int attachment,
	    int texture, int level, int face) {
	try{delegate.glFramebufferTextureFaceARB(target, attachment, texture,
		level, face);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockIndex
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformBlockiv(int, int, int, int[], int)
     */
    public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex,
	    int pname, int[] params, int params_offset) {
	try{delegate.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname,
		params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param ids
     * @see javax.media.opengl.GL2ES2#glGenQueries(int, java.nio.IntBuffer)
     */
    public void glGenQueries(int n, IntBuffer ids) {
	try{delegate.glGenQueries(n, ids);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param texture
     * @param level
     * @param layer
     * @see javax.media.opengl.GL2GL3#glFramebufferTextureLayerARB(int, int, int, int, int)
     */
    public void glFramebufferTextureLayerARB(int target, int attachment,
	    int texture, int level, int layer) {
	try{delegate.glFramebufferTextureLayerARB(target, attachment, texture,
		level, layer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformCount
     * @param uniformIndices
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformsiv(int, int, java.nio.IntBuffer, int, java.nio.IntBuffer)
     */
    public void glGetActiveUniformsiv(int program, int uniformCount,
	    IntBuffer uniformIndices, int pname, IntBuffer params) {
	try{delegate.glGetActiveUniformsiv(program, uniformCount, uniformIndices,
		pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param ids
     * @param ids_offset
     * @see javax.media.opengl.GL2ES2#glGenQueries(int, int[], int)
     */
    public void glGenQueries(int n, int[] ids, int ids_offset) {
	try{delegate.glGenQueries(n, ids, ids_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformIndex
     * @param bufSize
     * @param length
     * @param uniformName
     * @see javax.media.opengl.GL2GL3#glGetActiveUniformName(int, int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetActiveUniformName(int program, int uniformIndex,
	    int bufSize, IntBuffer length, ByteBuffer uniformName) {
	try{delegate.glGetActiveUniformName(program, uniformIndex, bufSize, length,
		uniformName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufsize
     * @param length
     * @param size
     * @param type
     * @param name
     * @see javax.media.opengl.GL2ES2#glGetActiveAttrib(int, int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetActiveAttrib(int program, int index, int bufsize,
	    IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
	try{delegate.glGetActiveAttrib(program, index, bufsize, length, size, type,
		name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformCount
     * @param uniformIndices
     * @param uniformIndices_offset
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetActiveUniformsiv(int, int, int[], int, int, int[], int)
     */
    public void glGetActiveUniformsiv(int program, int uniformCount,
	    int[] uniformIndices, int uniformIndices_offset, int pname,
	    int[] params, int params_offset) {
	try{delegate.glGetActiveUniformsiv(program, uniformCount, uniformIndices,
		uniformIndices_offset, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformIndex
     * @param bufSize
     * @param length
     * @param length_offset
     * @param uniformName
     * @param uniformName_offset
     * @see javax.media.opengl.GL2GL3#glGetActiveUniformName(int, int, int, int[], int, byte[], int)
     */
    public void glGetActiveUniformName(int program, int uniformIndex,
	    int bufSize, int[] length, int length_offset, byte[] uniformName,
	    int uniformName_offset) {
	try{delegate.glGetActiveUniformName(program, uniformIndex, bufSize, length,
		length_offset, uniformName, uniformName_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufsize
     * @param length
     * @param length_offset
     * @param size
     * @param size_offset
     * @param type
     * @param type_offset
     * @param name
     * @param name_offset
     * @see javax.media.opengl.GL2ES2#glGetActiveAttrib(int, int, int, int[], int, int[], int, int[], int, byte[], int)
     */
    public void glGetActiveAttrib(int program, int index, int bufsize,
	    int[] length, int length_offset, int[] size, int size_offset,
	    int[] type, int type_offset, byte[] name, int name_offset) {
	try{delegate.glGetActiveAttrib(program, index, bufsize, length,
		length_offset, size, size_offset, type, type_offset, name,
		name_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param name
     * @return
     * @see javax.media.opengl.GL2ES3#glGetFragDataLocation(int, java.lang.String)
     */
    public int glGetFragDataLocation(int program, String name) {
	try{ return delegate.glGetFragDataLocation(program, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @see javax.media.opengl.GL2GL3#glGetBooleani_v(int, int, java.nio.ByteBuffer)
     */
    public void glGetBooleani_v(int target, int index, ByteBuffer data) {
	try{delegate.glGetBooleani_v(target, index, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @see javax.media.opengl.GL2ES3#glGetIntegeri_v(int, int, java.nio.IntBuffer)
     */
    public void glGetIntegeri_v(int target, int index, IntBuffer data) {
	try{delegate.glGetIntegeri_v(target, index, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufsize
     * @param length
     * @param size
     * @param type
     * @param name
     * @see javax.media.opengl.GL2ES2#glGetActiveUniform(int, int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetActiveUniform(int program, int index, int bufsize,
	    IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
	try{delegate.glGetActiveUniform(program, index, bufsize, length, size,
		type, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @param data_offset
     * @see javax.media.opengl.GL2GL3#glGetBooleani_v(int, int, byte[], int)
     */
    public void glGetBooleani_v(int target, int index, byte[] data,
	    int data_offset) {
	try{delegate.glGetBooleani_v(target, index, data, data_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @param data
     * @param data_offset
     * @see javax.media.opengl.GL2ES3#glGetIntegeri_v(int, int, int[], int)
     */
    public void glGetIntegeri_v(int target, int index, int[] data,
	    int data_offset) {
	try{delegate.glGetIntegeri_v(target, index, data, data_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetBufferParameterui64vNV(int, int, java.nio.LongBuffer)
     */
    public void glGetBufferParameterui64vNV(int target, int pname,
	    LongBuffer params) {
	try{delegate.glGetBufferParameterui64vNV(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param internalformat
     * @param pname
     * @param bufSize
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetInternalformativ(int, int, int, int, java.nio.IntBuffer)
     */
    public void glGetInternalformativ(int target, int internalformat,
	    int pname, int bufSize, IntBuffer params) {
	try{delegate.glGetInternalformativ(target, internalformat, pname, bufSize,
		params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetBufferParameterui64vNV(int, int, long[], int)
     */
    public void glGetBufferParameterui64vNV(int target, int pname,
	    long[] params, int params_offset) {
	try{delegate.glGetBufferParameterui64vNV(target, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufsize
     * @param length
     * @param length_offset
     * @param size
     * @param size_offset
     * @param type
     * @param type_offset
     * @param name
     * @param name_offset
     * @see javax.media.opengl.GL2ES2#glGetActiveUniform(int, int, int, int[], int, int[], int, int[], int, byte[], int)
     */
    public void glGetActiveUniform(int program, int index, int bufsize,
	    int[] length, int length_offset, int[] size, int size_offset,
	    int[] type, int type_offset, byte[] name, int name_offset) {
	try{delegate.glGetActiveUniform(program, index, bufsize, length,
		length_offset, size, size_offset, type, type_offset, name,
		name_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param offset
     * @param size
     * @param data
     * @see javax.media.opengl.GL2GL3#glGetBufferSubData(int, long, long, java.nio.Buffer)
     */
    public void glGetBufferSubData(int target, long offset, long size,
	    Buffer data) {
	try{delegate.glGetBufferSubData(target, offset, size, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param internalformat
     * @param pname
     * @param bufSize
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetInternalformativ(int, int, int, int, int[], int)
     */
    public void glGetInternalformativ(int target, int internalformat,
	    int pname, int bufSize, int[] params, int params_offset) {
	try{delegate.glGetInternalformativ(target, internalformat, pname, bufSize,
		params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param maxcount
     * @param count
     * @param shaders
     * @see javax.media.opengl.GL2ES2#glGetAttachedShaders(int, int, java.nio.IntBuffer, java.nio.IntBuffer)
     */
    public void glGetAttachedShaders(int program, int maxcount,
	    IntBuffer count, IntBuffer shaders) {
	try{delegate.glGetAttachedShaders(program, maxcount, count, shaders);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param img
     * @see javax.media.opengl.GL2GL3#glGetCompressedTexImage(int, int, java.nio.Buffer)
     */
    public void glGetCompressedTexImage(int target, int level, Buffer img) {
	try{delegate.glGetCompressedTexImage(target, level, img);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param name
     * @param index
     * @return
     * @see javax.media.opengl.GL2ES3#glGetStringi(int, int)
     */
    public String glGetStringi(int name, int index) {
	try{ return delegate.glGetStringi(name, index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufSize
     * @param length
     * @param size
     * @param type
     * @param name
     * @see javax.media.opengl.GL2ES3#glGetTransformFeedbackVarying(int, int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetTransformFeedbackVarying(int program, int index,
	    int bufSize, IntBuffer length, IntBuffer size, IntBuffer type,
	    ByteBuffer name) {
	try{delegate.glGetTransformFeedbackVarying(program, index, bufSize, length,
		size, type, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param img_buffer_offset
     * @see javax.media.opengl.GL2GL3#glGetCompressedTexImage(int, int, long)
     */
    public void glGetCompressedTexImage(int target, int level,
	    long img_buffer_offset) {
	try{delegate.glGetCompressedTexImage(target, level, img_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param maxcount
     * @param count
     * @param count_offset
     * @param shaders
     * @param shaders_offset
     * @see javax.media.opengl.GL2ES2#glGetAttachedShaders(int, int, int[], int, int[], int)
     */
    public void glGetAttachedShaders(int program, int maxcount, int[] count,
	    int count_offset, int[] shaders, int shaders_offset) {
	try{delegate.glGetAttachedShaders(program, maxcount, count, count_offset,
		shaders, shaders_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param bufsize
     * @param categories
     * @param severities
     * @param ids
     * @param lengths
     * @param message
     * @return
     * @see javax.media.opengl.GL2GL3#glGetDebugMessageLogAMD(int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public int glGetDebugMessageLogAMD(int count, int bufsize,
	    IntBuffer categories, IntBuffer severities, IntBuffer ids,
	    IntBuffer lengths, ByteBuffer message) {
	try{ return delegate.glGetDebugMessageLogAMD(count, bufsize, categories,
		severities, ids, lengths, message);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param name
     * @return
     * @see javax.media.opengl.GL2ES2#glGetAttribLocation(int, java.lang.String)
     */
    public int glGetAttribLocation(int program, String name) {
	try{ return delegate.glGetAttribLocation(program, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param bufsize
     * @param sources
     * @param types
     * @param ids
     * @param severities
     * @param lengths
     * @param messageLog
     * @return
     * @see javax.media.opengl.GL2ES2#glGetDebugMessageLog(int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public int glGetDebugMessageLog(int count, int bufsize, IntBuffer sources,
	    IntBuffer types, IntBuffer ids, IntBuffer severities,
	    IntBuffer lengths, ByteBuffer messageLog) {
	try{ return delegate.glGetDebugMessageLog(count, bufsize, sources, types,
		ids, severities, lengths, messageLog);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param index
     * @param bufSize
     * @param length
     * @param length_offset
     * @param size
     * @param size_offset
     * @param type
     * @param type_offset
     * @param name
     * @param name_offset
     * @see javax.media.opengl.GL2ES3#glGetTransformFeedbackVarying(int, int, int, int[], int, int[], int, int[], int, byte[], int)
     */
    public void glGetTransformFeedbackVarying(int program, int index,
	    int bufSize, int[] length, int length_offset, int[] size,
	    int size_offset, int[] type, int type_offset, byte[] name,
	    int name_offset) {
	try{delegate.glGetTransformFeedbackVarying(program, index, bufSize, length,
		length_offset, size, size_offset, type, type_offset, name,
		name_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param bufsize
     * @param categories
     * @param categories_offset
     * @param severities
     * @param severities_offset
     * @param ids
     * @param ids_offset
     * @param lengths
     * @param lengths_offset
     * @param message
     * @param message_offset
     * @return
     * @see javax.media.opengl.GL2GL3#glGetDebugMessageLogAMD(int, int, int[], int, int[], int, int[], int, int[], int, byte[], int)
     */
    public int glGetDebugMessageLogAMD(int count, int bufsize,
	    int[] categories, int categories_offset, int[] severities,
	    int severities_offset, int[] ids, int ids_offset, int[] lengths,
	    int lengths_offset, byte[] message, int message_offset) {
	try{ return delegate.glGetDebugMessageLogAMD(count, bufsize, categories,
		categories_offset, severities, severities_offset, ids,
		ids_offset, lengths, lengths_offset, message, message_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockName
     * @return
     * @see javax.media.opengl.GL2ES3#glGetUniformBlockIndex(int, java.lang.String)
     */
    public int glGetUniformBlockIndex(int program, String uniformBlockName) {
	try{ return delegate.glGetUniformBlockIndex(program, uniformBlockName);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformCount
     * @param uniformNames
     * @param uniformIndices
     * @see javax.media.opengl.GL2ES3#glGetUniformIndices(int, int, java.lang.String[], java.nio.IntBuffer)
     */
    public void glGetUniformIndices(int program, int uniformCount,
	    String[] uniformNames, IntBuffer uniformIndices) {
	try{delegate.glGetUniformIndices(program, uniformCount, uniformNames,
		uniformIndices);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetDoublev(int, java.nio.DoubleBuffer)
     */
    public void glGetDoublev(int pname, DoubleBuffer params) {
	try{delegate.glGetDoublev(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param count
     * @param bufsize
     * @param sources
     * @param sources_offset
     * @param types
     * @param types_offset
     * @param ids
     * @param ids_offset
     * @param severities
     * @param severities_offset
     * @param lengths
     * @param lengths_offset
     * @param messageLog
     * @param messageLog_offset
     * @return
     * @see javax.media.opengl.GL2ES2#glGetDebugMessageLog(int, int, int[], int, int[], int, int[], int, int[], int, int[], int, byte[], int)
     */
    public int glGetDebugMessageLog(int count, int bufsize, int[] sources,
	    int sources_offset, int[] types, int types_offset, int[] ids,
	    int ids_offset, int[] severities, int severities_offset,
	    int[] lengths, int lengths_offset, byte[] messageLog,
	    int messageLog_offset) {
	try{ return delegate.glGetDebugMessageLog(count, bufsize, sources,
		sources_offset, types, types_offset, ids, ids_offset,
		severities, severities_offset, lengths, lengths_offset,
		messageLog, messageLog_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetDoublev(int, double[], int)
     */
    public void glGetDoublev(int pname, double[] params, int params_offset) {
	try{delegate.glGetDoublev(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformCount
     * @param uniformNames
     * @param uniformIndices
     * @param uniformIndices_offset
     * @see javax.media.opengl.GL2ES3#glGetUniformIndices(int, int, java.lang.String[], int[], int)
     */
    public void glGetUniformIndices(int program, int uniformCount,
	    String[] uniformNames, int[] uniformIndices,
	    int uniformIndices_offset) {
	try{delegate.glGetUniformIndices(program, uniformCount, uniformNames,
		uniformIndices, uniformIndices_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param value
     * @param index
     * @param result
     * @see javax.media.opengl.GL2GL3#glGetIntegerui64i_vNV(int, int, java.nio.LongBuffer)
     */
    public void glGetIntegerui64i_vNV(int value, int index, LongBuffer result) {
	try{delegate.glGetIntegerui64i_vNV(value, index, result);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param identifier
     * @param name
     * @param bufSize
     * @param length
     * @param label
     * @see javax.media.opengl.GL2ES2#glGetObjectLabel(int, int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetObjectLabel(int identifier, int name, int bufSize,
	    IntBuffer length, ByteBuffer label) {
	try{delegate.glGetObjectLabel(identifier, name, bufSize, length, label);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param value
     * @param index
     * @param result
     * @param result_offset
     * @see javax.media.opengl.GL2GL3#glGetIntegerui64i_vNV(int, int, long[], int)
     */
    public void glGetIntegerui64i_vNV(int value, int index, long[] result,
	    int result_offset) {
	try{delegate.glGetIntegerui64i_vNV(value, index, result, result_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetUniformuiv(int, int, java.nio.IntBuffer)
     */
    public void glGetUniformuiv(int program, int location, IntBuffer params) {
	try{delegate.glGetUniformuiv(program, location, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param value
     * @param result
     * @see javax.media.opengl.GL2GL3#glGetIntegerui64vNV(int, java.nio.LongBuffer)
     */
    public void glGetIntegerui64vNV(int value, LongBuffer result) {
	try{delegate.glGetIntegerui64vNV(value, result);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param identifier
     * @param name
     * @param bufSize
     * @param length
     * @param length_offset
     * @param label
     * @param label_offset
     * @see javax.media.opengl.GL2ES2#glGetObjectLabel(int, int, int, int[], int, byte[], int)
     */
    public void glGetObjectLabel(int identifier, int name, int bufSize,
	    int[] length, int length_offset, byte[] label, int label_offset) {
	try{delegate.glGetObjectLabel(identifier, name, bufSize, length,
		length_offset, label, label_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetUniformuiv(int, int, int[], int)
     */
    public void glGetUniformuiv(int program, int location, int[] params,
	    int params_offset) {
	try{delegate.glGetUniformuiv(program, location, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param value
     * @param result
     * @param result_offset
     * @see javax.media.opengl.GL2GL3#glGetIntegerui64vNV(int, long[], int)
     */
    public void glGetIntegerui64vNV(int value, long[] result, int result_offset) {
	try{delegate.glGetIntegerui64vNV(value, result, result_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param ptr
     * @param bufSize
     * @param length
     * @param label
     * @see javax.media.opengl.GL2ES2#glGetObjectPtrLabel(java.nio.Buffer, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, IntBuffer length,
	    ByteBuffer label) {
	try{delegate.glGetObjectPtrLabel(ptr, bufSize, length, label);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetVertexAttribIiv(int, int, java.nio.IntBuffer)
     */
    public void glGetVertexAttribIiv(int index, int pname, IntBuffer params) {
	try{delegate.glGetVertexAttribIiv(index, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetNamedBufferParameterui64vNV(int, int, java.nio.LongBuffer)
     */
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname,
	    LongBuffer params) {
	try{delegate.glGetNamedBufferParameterui64vNV(buffer, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetVertexAttribIiv(int, int, int[], int)
     */
    public void glGetVertexAttribIiv(int index, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetVertexAttribIiv(index, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetNamedBufferParameterui64vNV(int, int, long[], int)
     */
    public void glGetNamedBufferParameterui64vNV(int buffer, int pname,
	    long[] params, int params_offset) {
	try{delegate.glGetNamedBufferParameterui64vNV(buffer, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param ptr
     * @param bufSize
     * @param length
     * @param length_offset
     * @param label
     * @param label_offset
     * @see javax.media.opengl.GL2ES2#glGetObjectPtrLabel(java.nio.Buffer, int, int[], int, byte[], int)
     */
    public void glGetObjectPtrLabel(Buffer ptr, int bufSize, int[] length,
	    int length_offset, byte[] label, int label_offset) {
	try{delegate.glGetObjectPtrLabel(ptr, bufSize, length, length_offset,
		label, label_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES3#glGetVertexAttribIuiv(int, int, java.nio.IntBuffer)
     */
    public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params) {
	try{delegate.glGetVertexAttribIuiv(index, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param framebuffer
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetNamedFramebufferParameterivEXT(int, int, java.nio.IntBuffer)
     */
    public void glGetNamedFramebufferParameterivEXT(int framebuffer, int pname,
	    IntBuffer params) {
	try{delegate.glGetNamedFramebufferParameterivEXT(framebuffer, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param bufSize
     * @param length
     * @param binaryFormat
     * @param binary
     * @see javax.media.opengl.GL2ES2#glGetProgramBinary(int, int, java.nio.IntBuffer, java.nio.IntBuffer, java.nio.Buffer)
     */
    public void glGetProgramBinary(int program, int bufSize, IntBuffer length,
	    IntBuffer binaryFormat, Buffer binary) {
	try{delegate.glGetProgramBinary(program, bufSize, length, binaryFormat,
		binary);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES3#glGetVertexAttribIuiv(int, int, int[], int)
     */
    public void glGetVertexAttribIuiv(int index, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetVertexAttribIuiv(index, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param framebuffer
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetNamedFramebufferParameterivEXT(int, int, int[], int)
     */
    public void glGetNamedFramebufferParameterivEXT(int framebuffer, int pname,
	    int[] params, int params_offset) {
	try{delegate.glGetNamedFramebufferParameterivEXT(framebuffer, pname,
		params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param array
     * @return
     * @see javax.media.opengl.GL2ES3#glIsVertexArray(int)
     */
    public boolean glIsVertexArray(int array) {
	try{ return delegate.glIsVertexArray(array);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @param bufSize
     * @param stringlen
     * @param string
     * @see javax.media.opengl.GL2GL3#glGetNamedStringARB(int, java.lang.String, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetNamedStringARB(int namelen, String name, int bufSize,
	    IntBuffer stringlen, ByteBuffer string) {
	try{delegate.glGetNamedStringARB(namelen, name, bufSize, stringlen, string);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL2ES3#glReadBuffer(int)
     */
    public void glReadBuffer(int mode) {
	try{delegate.glReadBuffer(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param bufSize
     * @param length
     * @param length_offset
     * @param binaryFormat
     * @param binaryFormat_offset
     * @param binary
     * @see javax.media.opengl.GL2ES2#glGetProgramBinary(int, int, int[], int, int[], int, java.nio.Buffer)
     */
    public void glGetProgramBinary(int program, int bufSize, int[] length,
	    int length_offset, int[] binaryFormat, int binaryFormat_offset,
	    Buffer binary) {
	try{delegate.glGetProgramBinary(program, bufSize, length, length_offset,
		binaryFormat, binaryFormat_offset, binary);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param samples
     * @param internalformat
     * @param width
     * @param height
     * @see javax.media.opengl.GL2ES3#glRenderbufferStorageMultisample(int, int, int, int, int)
     */
    public void glRenderbufferStorageMultisample(int target, int samples,
	    int internalformat, int width, int height) {
	try{delegate.glRenderbufferStorageMultisample(target, samples,
		internalformat, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @param bufSize
     * @param stringlen
     * @param stringlen_offset
     * @param string
     * @param string_offset
     * @see javax.media.opengl.GL2GL3#glGetNamedStringARB(int, java.lang.String, int, int[], int, byte[], int)
     */
    public void glGetNamedStringARB(int namelen, String name, int bufSize,
	    int[] stringlen, int stringlen_offset, byte[] string,
	    int string_offset) {
	try{delegate.glGetNamedStringARB(namelen, name, bufSize, stringlen,
		stringlen_offset, string, string_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param count
     * @param varyings
     * @param bufferMode
     * @see javax.media.opengl.GL2ES3#glTransformFeedbackVaryings(int, int, java.lang.String[], int)
     */
    public void glTransformFeedbackVaryings(int program, int count,
	    String[] varyings, int bufferMode) {
	try{delegate.glTransformFeedbackVaryings(program, count, varyings,
		bufferMode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param bufsize
     * @param length
     * @param infolog
     * @see javax.media.opengl.GL2ES2#glGetProgramInfoLog(int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetProgramInfoLog(int program, int bufsize, IntBuffer length,
	    ByteBuffer infolog) {
	try{delegate.glGetProgramInfoLog(program, bufsize, length, infolog);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetNamedStringivARB(int, java.lang.String, int, java.nio.IntBuffer)
     */
    public void glGetNamedStringivARB(int namelen, String name, int pname,
	    IntBuffer params) {
	try{delegate.glGetNamedStringivARB(namelen, name, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param v0
     * @see javax.media.opengl.GL2ES3#glUniform1ui(int, int)
     */
    public void glUniform1ui(int location, int v0) {
	try{delegate.glUniform1ui(location, v0);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param bufsize
     * @param length
     * @param length_offset
     * @param infolog
     * @param infolog_offset
     * @see javax.media.opengl.GL2ES2#glGetProgramInfoLog(int, int, int[], int, byte[], int)
     */
    public void glGetProgramInfoLog(int program, int bufsize, int[] length,
	    int length_offset, byte[] infolog, int infolog_offset) {
	try{delegate.glGetProgramInfoLog(program, bufsize, length, length_offset,
		infolog, infolog_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetNamedStringivARB(int, java.lang.String, int, int[], int)
     */
    public void glGetNamedStringivARB(int namelen, String name, int pname,
	    int[] params, int params_offset) {
	try{delegate.glGetNamedStringivARB(namelen, name, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniform1uiv(int, int, java.nio.IntBuffer)
     */
    public void glUniform1uiv(int location, int count, IntBuffer value) {
	try{delegate.glUniform1uiv(location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetProgramiv(int, int, java.nio.IntBuffer)
     */
    public void glGetProgramiv(int program, int pname, IntBuffer params) {
	try{delegate.glGetProgramiv(program, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetQueryObjectiv(int, int, java.nio.IntBuffer)
     */
    public void glGetQueryObjectiv(int id, int pname, IntBuffer params) {
	try{delegate.glGetQueryObjectiv(id, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniform1uiv(int, int, int[], int)
     */
    public void glUniform1uiv(int location, int count, int[] value,
	    int value_offset) {
	try{delegate.glUniform1uiv(location, count, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetProgramiv(int, int, int[], int)
     */
    public void glGetProgramiv(int program, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetProgramiv(program, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetQueryObjectiv(int, int, int[], int)
     */
    public void glGetQueryObjectiv(int id, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetQueryObjectiv(id, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param v0
     * @param v1
     * @see javax.media.opengl.GL2ES3#glUniform2ui(int, int, int)
     */
    public void glUniform2ui(int location, int v0, int v1) {
	try{delegate.glUniform2ui(location, v0, v1);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL2GL3#glGetTexImage(int, int, int, int, java.nio.Buffer)
     */
    public void glGetTexImage(int target, int level, int format, int type,
	    Buffer pixels) {
	try{delegate.glGetTexImage(target, level, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetQueryObjectuiv(int, int, java.nio.IntBuffer)
     */
    public void glGetQueryObjectuiv(int id, int pname, IntBuffer params) {
	try{delegate.glGetQueryObjectuiv(id, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @see javax.media.opengl.GL#glActiveTexture(int)
     */
    public void glActiveTexture(int texture) {
	
	try{delegate.glActiveTexture(texture);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniform2uiv(int, int, java.nio.IntBuffer)
     */
    public void glUniform2uiv(int location, int count, IntBuffer value) {
	try{delegate.glUniform2uiv(location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL2GL3#glGetTexImage(int, int, int, int, long)
     */
    public void glGetTexImage(int target, int level, int format, int type,
	    long pixels_buffer_offset) {
	try{delegate.glGetTexImage(target, level, format, type,
		pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param buffer
     * @see javax.media.opengl.GL#glBindBuffer(int, int)
     */
    public void glBindBuffer(int target, int buffer) {
	try{delegate.glBindBuffer(target, buffer);} catch(GLException e){glException(e); throw e;}
	switch(target){
	case GL3.GL_ARRAY_BUFFER:
	    bean.setArrayBufferBinding(buffer);
	    break;
	case GL3.GL_TEXTURE_BUFFER:
	    bean.setTextureBufferBufferBinding(buffer);
	    break;
	default:
	 unhandledState("Unimplemented handler for buffer target "+target);
	}
    }
    /**
     * @param id
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetQueryObjectuiv(int, int, int[], int)
     */
    public void glGetQueryObjectuiv(int id, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetQueryObjectuiv(id, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniform2uiv(int, int, int[], int)
     */
    public void glUniform2uiv(int location, int count, int[] value,
	    int value_offset) {
	try{delegate.glUniform2uiv(location, count, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param framebuffer
     * @see javax.media.opengl.GL#glBindFramebuffer(int, int)
     */
    public void glBindFramebuffer(int target, int framebuffer) {
	try{delegate.glBindFramebuffer(target, framebuffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetTexLevelParameterfv(int, int, int, java.nio.FloatBuffer)
     */
    public void glGetTexLevelParameterfv(int target, int level, int pname,
	    FloatBuffer params) {
	try{delegate.glGetTexLevelParameterfv(target, level, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetQueryiv(int, int, java.nio.IntBuffer)
     */
    public void glGetQueryiv(int target, int pname, IntBuffer params) {
	try{delegate.glGetQueryiv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param v0
     * @param v1
     * @param v2
     * @see javax.media.opengl.GL2ES3#glUniform3ui(int, int, int, int)
     */
    public void glUniform3ui(int location, int v0, int v1, int v2) {
	try{delegate.glUniform3ui(location, v0, v1, v2);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param renderbuffer
     * @see javax.media.opengl.GL#glBindRenderbuffer(int, int)
     */
    public void glBindRenderbuffer(int target, int renderBuffer) {
	switch(target){
	case GL3.GL_DRAW_FRAMEBUFFER:
	    bean.setDrawRenderBufferBinding(renderBuffer);
	    break;
	case GL3.GL_READ_FRAMEBUFFER:
	    bean.setReadRenderBufferBinding(renderBuffer);
	    break;
	    default:
		unhandledState("Unhandled renderbuffer target: "+target);
	}
	try{delegate.glBindRenderbuffer(target, renderBuffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetTexLevelParameterfv(int, int, int, float[], int)
     */
    public void glGetTexLevelParameterfv(int target, int level, int pname,
	    float[] params, int params_offset) {
	try{delegate.glGetTexLevelParameterfv(target, level, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniform3uiv(int, int, java.nio.IntBuffer)
     */
    public void glUniform3uiv(int location, int count, IntBuffer value) {
	try{delegate.glUniform3uiv(location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetQueryiv(int, int, int[], int)
     */
    public void glGetQueryiv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetQueryiv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param texture
     * @see javax.media.opengl.GL#glBindTexture(int, int)
     */
    public void glBindTexture(int target, int texture) {
	try{delegate.glBindTexture(target, texture);} catch(GLException e){glException(e); throw e;}
	switch(target){
	case GL3.GL_TEXTURE_1D:
	    bean.setTexture1DBinding(texture);
	    break;
	case GL3.GL_TEXTURE_2D:
	    bean.setTexture2DBinding(texture);
	    break;
	case GL3.GL_TEXTURE_2D_MULTISAMPLE:
	    bean.setTexture2DMSBinding(texture);
	    break;
	case GL3.GL_TEXTURE_2D_ARRAY:
	    bean.setTexture2DArrayBinding(texture);
	    break;
	case GL3.GL_TEXTURE_BUFFER:
	    bean.setTextureBufferTextureBinding(texture);
	    break;
	default:
	    unhandledState("Could not identify binding target: "+target);
	}
    }
    /**
     * @param target
     * @param level
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetTexLevelParameteriv(int, int, int, java.nio.IntBuffer)
     */
    public void glGetTexLevelParameteriv(int target, int level, int pname,
	    IntBuffer params) {
	try{delegate.glGetTexLevelParameteriv(target, level, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniform3uiv(int, int, int[], int)
     */
    public void glUniform3uiv(int location, int count, int[] value,
	    int value_offset) {
	try{delegate.glUniform3uiv(location, count, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL#glBlendEquation(int)
     */
    public void glBlendEquation(int mode) {
	try{delegate.glBlendEquation(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param bufsize
     * @param length
     * @param infolog
     * @see javax.media.opengl.GL2ES2#glGetShaderInfoLog(int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetShaderInfoLog(int shader, int bufsize, IntBuffer length,
	    ByteBuffer infolog) {
	try{delegate.glGetShaderInfoLog(shader, bufsize, length, infolog);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetTexLevelParameteriv(int, int, int, int[], int)
     */
    public void glGetTexLevelParameteriv(int target, int level, int pname,
	    int[] params, int params_offset) {
	try{delegate.glGetTexLevelParameteriv(target, level, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param modeRGB
     * @param modeAlpha
     * @see javax.media.opengl.GL#glBlendEquationSeparate(int, int)
     */
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
	try{delegate.glBlendEquationSeparate(modeRGB, modeAlpha);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param v0
     * @param v1
     * @param v2
     * @param v3
     * @see javax.media.opengl.GL2ES3#glUniform4ui(int, int, int, int, int)
     */
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
	try{delegate.glUniform4ui(location, v0, v1, v2, v3);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetTexParameterIiv(int, int, java.nio.IntBuffer)
     */
    public void glGetTexParameterIiv(int target, int pname, IntBuffer params) {
	try{delegate.glGetTexParameterIiv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param bufsize
     * @param length
     * @param length_offset
     * @param infolog
     * @param infolog_offset
     * @see javax.media.opengl.GL2ES2#glGetShaderInfoLog(int, int, int[], int, byte[], int)
     */
    public void glGetShaderInfoLog(int shader, int bufsize, int[] length,
	    int length_offset, byte[] infolog, int infolog_offset) {
	try{delegate.glGetShaderInfoLog(shader, bufsize, length, length_offset,
		infolog, infolog_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param sfactor
     * @param dfactor
     * @see javax.media.opengl.GL#glBlendFunc(int, int)
     */
    public void glBlendFunc(int sfactor, int dfactor) {
	try{delegate.glBlendFunc(sfactor, dfactor);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniform4uiv(int, int, java.nio.IntBuffer)
     */
    public void glUniform4uiv(int location, int count, IntBuffer value) {
	try{delegate.glUniform4uiv(location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetTexParameterIiv(int, int, int[], int)
     */
    public void glGetTexParameterIiv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetTexParameterIiv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param srcRGB
     * @param dstRGB
     * @param srcAlpha
     * @param dstAlpha
     * @see javax.media.opengl.GL#glBlendFuncSeparate(int, int, int, int)
     */
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha,
	    int dstAlpha) {
	try{delegate.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param bufsize
     * @param length
     * @param source
     * @see javax.media.opengl.GL2ES2#glGetShaderSource(int, int, java.nio.IntBuffer, java.nio.ByteBuffer)
     */
    public void glGetShaderSource(int shader, int bufsize, IntBuffer length,
	    ByteBuffer source) {
	try{delegate.glGetShaderSource(shader, bufsize, length, source);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniform4uiv(int, int, int[], int)
     */
    public void glUniform4uiv(int location, int count, int[] value,
	    int value_offset) {
	try{delegate.glUniform4uiv(location, count, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetTexParameterIuiv(int, int, java.nio.IntBuffer)
     */
    public void glGetTexParameterIuiv(int target, int pname, IntBuffer params) {
	try{delegate.glGetTexParameterIuiv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param size
     * @param data
     * @param usage
     * @see javax.media.opengl.GL#glBufferData(int, long, java.nio.Buffer, int)
     */
    public void glBufferData(int target, long size, Buffer data, int usage) {
	try{delegate.glBufferData(target, size, data, usage);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param bufsize
     * @param length
     * @param length_offset
     * @param source
     * @param source_offset
     * @see javax.media.opengl.GL2ES2#glGetShaderSource(int, int, int[], int, byte[], int)
     */
    public void glGetShaderSource(int shader, int bufsize, int[] length,
	    int length_offset, byte[] source, int source_offset) {
	try{delegate.glGetShaderSource(shader, bufsize, length, length_offset,
		source, source_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param uniformBlockIndex
     * @param uniformBlockBinding
     * @see javax.media.opengl.GL2ES3#glUniformBlockBinding(int, int, int)
     */
    public void glUniformBlockBinding(int program, int uniformBlockIndex,
	    int uniformBlockBinding) {
	try{delegate.glUniformBlockBinding(program, uniformBlockIndex,
		uniformBlockBinding);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetTexParameterIuiv(int, int, int[], int)
     */
    public void glGetTexParameterIuiv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetTexParameterIuiv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param offset
     * @param size
     * @param data
     * @see javax.media.opengl.GL#glBufferSubData(int, long, long, java.nio.Buffer)
     */
    public void glBufferSubData(int target, long offset, long size, Buffer data) {
	try{delegate.glBufferSubData(target, offset, size, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetShaderiv(int, int, java.nio.IntBuffer)
     */
    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
	try{delegate.glGetShaderiv(shader, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix2x3fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix2x3fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix2x3fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetUniformui64vNV(int, int, java.nio.LongBuffer)
     */
    public void glGetUniformui64vNV(int program, int location, LongBuffer params) {
	try{delegate.glGetUniformui64vNV(program, location, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @return
     * @see javax.media.opengl.GL#glCheckFramebufferStatus(int)
     */
    public int glCheckFramebufferStatus(int target) {
	try{ return delegate.glCheckFramebufferStatus(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetShaderiv(int, int, int[], int)
     */
    public void glGetShaderiv(int shader, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetShaderiv(shader, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetUniformui64vNV(int, int, long[], int)
     */
    public void glGetUniformui64vNV(int program, int location, long[] params,
	    int params_offset) {
	try{delegate.glGetUniformui64vNV(program, location, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix2x3fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix2x3fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix2x3fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param name
     * @return
     * @see javax.media.opengl.GL2ES2#glGetUniformLocation(int, java.lang.String)
     */
    public int glGetUniformLocation(int program, String name) {
	try{ return delegate.glGetUniformLocation(program, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mask
     * @see javax.media.opengl.GL#glClear(int)
     */
    public void glClear(int mask) {
	try{delegate.glClear(mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetVertexAttribdv(int, int, java.nio.DoubleBuffer)
     */
    public void glGetVertexAttribdv(int index, int pname, DoubleBuffer params) {
	try{delegate.glGetVertexAttribdv(index, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix2x4fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix2x4fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix2x4fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @see javax.media.opengl.GL#glClearColor(float, float, float, float)
     */
    public void glClearColor(float red, float green, float blue, float alpha) {
	try{delegate.glClearColor(red, green, blue, alpha);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetUniformfv(int, int, java.nio.FloatBuffer)
     */
    public void glGetUniformfv(int program, int location, FloatBuffer params) {
	try{delegate.glGetUniformfv(program, location, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetVertexAttribdv(int, int, double[], int)
     */
    public void glGetVertexAttribdv(int index, int pname, double[] params,
	    int params_offset) {
	try{delegate.glGetVertexAttribdv(index, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix2x4fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix2x4fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix2x4fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetUniformfv(int, int, float[], int)
     */
    public void glGetUniformfv(int program, int location, float[] params,
	    int params_offset) {
	try{delegate.glGetUniformfv(program, location, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param lod
     * @param bufSize
     * @param img
     * @see javax.media.opengl.GL2GL3#glGetnCompressedTexImage(int, int, int, java.nio.Buffer)
     */
    public void glGetnCompressedTexImage(int target, int lod, int bufSize,
	    Buffer img) {
	try{delegate.glGetnCompressedTexImage(target, lod, bufSize, img);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param s
     * @see javax.media.opengl.GL#glClearStencil(int)
     */
    public void glClearStencil(int s) {
	try{delegate.glClearStencil(s);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix3x2fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix3x2fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix3x2fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetUniformiv(int, int, java.nio.IntBuffer)
     */
    public void glGetUniformiv(int program, int location, IntBuffer params) {
	try{delegate.glGetUniformiv(program, location, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param format
     * @param type
     * @param bufSize
     * @param img
     * @see javax.media.opengl.GL2GL3#glGetnTexImage(int, int, int, int, int, java.nio.Buffer)
     */
    public void glGetnTexImage(int target, int level, int format, int type,
	    int bufSize, Buffer img) {
	try{delegate.glGetnTexImage(target, level, format, type, bufSize, img);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @see javax.media.opengl.GL#glColorMask(boolean, boolean, boolean, boolean)
     */
    public void glColorMask(boolean red, boolean green, boolean blue,
	    boolean alpha) {
	try{delegate.glColorMask(red, green, blue, alpha);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetUniformiv(int, int, int[], int)
     */
    public void glGetUniformiv(int program, int location, int[] params,
	    int params_offset) {
	try{delegate.glGetUniformiv(program, location, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix3x2fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix3x2fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix3x2fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetnUniformdv(int, int, int, java.nio.DoubleBuffer)
     */
    public void glGetnUniformdv(int program, int location, int bufSize,
	    DoubleBuffer params) {
	try{delegate.glGetnUniformdv(program, location, bufSize, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param border
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexImage2D(int target, int level,
	    int internalformat, int width, int height, int border,
	    int imageSize, Buffer data) {
	try{delegate.glCompressedTexImage2D(target, level, internalformat, width,
		height, border, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetVertexAttribfv(int, int, java.nio.FloatBuffer)
     */
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
	try{delegate.glGetVertexAttribfv(index, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix3x4fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix3x4fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix3x4fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetnUniformdv(int, int, int, double[], int)
     */
    public void glGetnUniformdv(int program, int location, int bufSize,
	    double[] params, int params_offset) {
	try{delegate.glGetnUniformdv(program, location, bufSize, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetVertexAttribfv(int, int, float[], int)
     */
    public void glGetVertexAttribfv(int index, int pname, float[] params,
	    int params_offset) {
	try{delegate.glGetVertexAttribfv(index, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param border
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL#glCompressedTexImage2D(int, int, int, int, int, int, int, long)
     */
    public void glCompressedTexImage2D(int target, int level,
	    int internalformat, int width, int height, int border,
	    int imageSize, long data_buffer_offset) {
	try{delegate.glCompressedTexImage2D(target, level, internalformat, width,
		height, border, imageSize, data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @see javax.media.opengl.GL2GL3#glGetnUniformuiv(int, int, int, java.nio.IntBuffer)
     */
    public void glGetnUniformuiv(int program, int location, int bufSize,
	    IntBuffer params) {
	try{delegate.glGetnUniformuiv(program, location, bufSize, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix3x4fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix3x4fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix3x4fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2ES2#glGetVertexAttribiv(int, int, java.nio.IntBuffer)
     */
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
	try{delegate.glGetVertexAttribiv(index, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix4x2fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix4x2fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix4x2fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glGetnUniformuiv(int, int, int, int[], int)
     */
    public void glGetnUniformuiv(int program, int location, int bufSize,
	    int[] params, int params_offset) {
	try{delegate.glGetnUniformuiv(program, location, bufSize, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param width
     * @param height
     * @param format
     * @param imageSize
     * @param data
     * @see javax.media.opengl.GL#glCompressedTexSubImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glCompressedTexSubImage2D(int target, int level, int xoffset,
	    int yoffset, int width, int height, int format, int imageSize,
	    Buffer data) {
	try{delegate.glCompressedTexSubImage2D(target, level, xoffset, yoffset,
		width, height, format, imageSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2ES2#glGetVertexAttribiv(int, int, int[], int)
     */
    public void glGetVertexAttribiv(int index, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetVertexAttribiv(index, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param external_sync_type
     * @param external_sync
     * @param flags
     * @return
     * @see javax.media.opengl.GL2GL3#glImportSyncEXT(int, long, int)
     */
    public long glImportSyncEXT(int external_sync_type, long external_sync,
	    int flags) {
	try{ return delegate.glImportSyncEXT(external_sync_type, external_sync,
		flags);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix4x2fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix4x2fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix4x2fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @return
     * @see javax.media.opengl.GL2ES2#glIsProgram(int)
     */
    public boolean glIsProgram(int program) {
	try{ return delegate.glIsProgram(program);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glIndexFormatNV(int, int)
     */
    public void glIndexFormatNV(int type, int stride) {
	try{delegate.glIndexFormatNV(type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param width
     * @param height
     * @param format
     * @param imageSize
     * @param data_buffer_offset
     * @see javax.media.opengl.GL#glCompressedTexSubImage2D(int, int, int, int, int, int, int, int, long)
     */
    public void glCompressedTexSubImage2D(int target, int level, int xoffset,
	    int yoffset, int width, int height, int format, int imageSize,
	    long data_buffer_offset) {
	try{delegate.glCompressedTexSubImage2D(target, level, xoffset, yoffset,
		width, height, format, imageSize, data_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param id
     * @return
     * @see javax.media.opengl.GL2ES2#glIsQuery(int)
     */
    public boolean glIsQuery(int id) {
	try{ return delegate.glIsQuery(id);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES3#glUniformMatrix4x3fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix4x3fv(int location, int count,
	    boolean transpose, FloatBuffer value) {
	try{delegate.glUniformMatrix4x3fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @return
     * @see javax.media.opengl.GL2GL3#glIsBufferResidentNV(int)
     */
    public boolean glIsBufferResidentNV(int target) {
	try{ return delegate.glIsBufferResidentNV(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @return
     * @see javax.media.opengl.GL2ES2#glIsShader(int)
     */
    public boolean glIsShader(int shader) {
	try{ return delegate.glIsShader(shader);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param index
     * @return
     * @see javax.media.opengl.GL2GL3#glIsEnabledi(int, int)
     */
    public boolean glIsEnabledi(int target, int index) {
	try{ return delegate.glIsEnabledi(target, index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param x
     * @param y
     * @param width
     * @param height
     * @param border
     * @see javax.media.opengl.GL#glCopyTexImage2D(int, int, int, int, int, int, int, int)
     */
    public void glCopyTexImage2D(int target, int level, int internalformat,
	    int x, int y, int width, int height, int border) {
	try{delegate.glCopyTexImage2D(target, level, internalformat, x, y, width,
		height, border);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES3#glUniformMatrix4x3fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix4x3fv(int location, int count,
	    boolean transpose, float[] value, int value_offset) {
	try{delegate.glUniformMatrix4x3fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @see javax.media.opengl.GL2ES2#glLinkProgram(int)
     */
    public void glLinkProgram(int program) {
	try{delegate.glLinkProgram(program);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @return
     * @see javax.media.opengl.GL2GL3#glIsNamedBufferResidentNV(int)
     */
    public boolean glIsNamedBufferResidentNV(int buffer) {
	try{ return delegate.glIsNamedBufferResidentNV(buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param identifier
     * @param name
     * @param length
     * @param label
     * @see javax.media.opengl.GL2ES2#glObjectLabel(int, int, int, java.nio.ByteBuffer)
     */
    public void glObjectLabel(int identifier, int name, int length,
	    ByteBuffer label) {
	try{delegate.glObjectLabel(identifier, name, length, label);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param namelen
     * @param name
     * @return
     * @see javax.media.opengl.GL2GL3#glIsNamedStringARB(int, java.lang.String)
     */
    public boolean glIsNamedStringARB(int namelen, String name) {
	try{ return delegate.glIsNamedStringARB(namelen, name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4i(int, int, int, int, int)
     */
    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
	try{delegate.glVertexAttribI4i(index, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param x
     * @param y
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glCopyTexSubImage2D(int, int, int, int, int, int, int, int)
     */
    public void glCopyTexSubImage2D(int target, int level, int xoffset,
	    int yoffset, int x, int y, int width, int height) {
	try{delegate.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y,
		width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param opcode
     * @see javax.media.opengl.GL2GL3#glLogicOp(int)
     */
    public void glLogicOp(int opcode) {
	try{delegate.glLogicOp(opcode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4iv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI4iv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI4iv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param identifier
     * @param name
     * @param length
     * @param label
     * @param label_offset
     * @see javax.media.opengl.GL2ES2#glObjectLabel(int, int, int, byte[], int)
     */
    public void glObjectLabel(int identifier, int name, int length,
	    byte[] label, int label_offset) {
	try{delegate.glObjectLabel(identifier, name, length, label, label_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @see javax.media.opengl.GL2GL3#glMakeBufferNonResidentNV(int)
     */
    public void glMakeBufferNonResidentNV(int target) {
	try{delegate.glMakeBufferNonResidentNV(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL#glCullFace(int)
     */
    public void glCullFace(int mode) {
	try{delegate.glCullFace(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4iv(int, int[], int)
     */
    public void glVertexAttribI4iv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI4iv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param ptr
     * @param length
     * @param label
     * @see javax.media.opengl.GL2ES2#glObjectPtrLabel(java.nio.Buffer, int, java.nio.ByteBuffer)
     */
    public void glObjectPtrLabel(Buffer ptr, int length, ByteBuffer label) {
	try{delegate.glObjectPtrLabel(ptr, length, label);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param access
     * @see javax.media.opengl.GL2GL3#glMakeBufferResidentNV(int, int)
     */
    public void glMakeBufferResidentNV(int target, int access) {
	try{delegate.glMakeBufferResidentNV(target, access);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param buffers
     * @see javax.media.opengl.GL#glDeleteBuffers(int, java.nio.IntBuffer)
     */
    public void glDeleteBuffers(int n, IntBuffer buffers) {
	try{delegate.glDeleteBuffers(n, buffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4ui(int, int, int, int, int)
     */
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
	try{delegate.glVertexAttribI4ui(index, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @see javax.media.opengl.GL2GL3#glMakeNamedBufferNonResidentNV(int)
     */
    public void glMakeNamedBufferNonResidentNV(int buffer) {
	try{delegate.glMakeNamedBufferNonResidentNV(buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param buffers
     * @param buffers_offset
     * @see javax.media.opengl.GL#glDeleteBuffers(int, int[], int)
     */
    public void glDeleteBuffers(int n, int[] buffers, int buffers_offset) {
	try{delegate.glDeleteBuffers(n, buffers, buffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param ptr
     * @param length
     * @param label
     * @param label_offset
     * @see javax.media.opengl.GL2ES2#glObjectPtrLabel(java.nio.Buffer, int, byte[], int)
     */
    public void glObjectPtrLabel(Buffer ptr, int length, byte[] label,
	    int label_offset) {
	try{delegate.glObjectPtrLabel(ptr, length, label, label_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @param access
     * @see javax.media.opengl.GL2GL3#glMakeNamedBufferResidentNV(int, int)
     */
    public void glMakeNamedBufferResidentNV(int buffer, int access) {
	try{delegate.glMakeNamedBufferResidentNV(buffer, access);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4uiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI4uiv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI4uiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param framebuffers
     * @see javax.media.opengl.GL#glDeleteFramebuffers(int, java.nio.IntBuffer)
     */
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
	try{delegate.glDeleteFramebuffers(n, framebuffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param first
     * @param count
     * @param drawcount
     * @see javax.media.opengl.GL2GL3#glMultiDrawArrays(int, java.nio.IntBuffer, java.nio.IntBuffer, int)
     */
    public void glMultiDrawArrays(int mode, IntBuffer first, IntBuffer count,
	    int drawcount) {
	try{delegate.glMultiDrawArrays(mode, first, count, drawcount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL2ES2#glPopDebugGroup()
     */
    public void glPopDebugGroup() {
	try{delegate.glPopDebugGroup();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES3#glVertexAttribI4uiv(int, int[], int)
     */
    public void glVertexAttribI4uiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI4uiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param binaryFormat
     * @param binary
     * @param length
     * @see javax.media.opengl.GL2ES2#glProgramBinary(int, int, java.nio.Buffer, int)
     */
    public void glProgramBinary(int program, int binaryFormat, Buffer binary,
	    int length) {
	try{delegate.glProgramBinary(program, binaryFormat, binary, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param size
     * @param type
     * @param stride
     * @param pointer
     * @see javax.media.opengl.GL2ES3#glVertexAttribIPointer(int, int, int, int, java.nio.Buffer)
     */
    public void glVertexAttribIPointer(int index, int size, int type,
	    int stride, Buffer pointer) {
	try{delegate.glVertexAttribIPointer(index, size, type, stride, pointer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param framebuffers
     * @param framebuffers_offset
     * @see javax.media.opengl.GL#glDeleteFramebuffers(int, int[], int)
     */
    public void glDeleteFramebuffers(int n, int[] framebuffers,
	    int framebuffers_offset) {
	try{delegate.glDeleteFramebuffers(n, framebuffers, framebuffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param first
     * @param first_offset
     * @param count
     * @param count_offset
     * @param drawcount
     * @see javax.media.opengl.GL2GL3#glMultiDrawArrays(int, int[], int, int[], int, int)
     */
    public void glMultiDrawArrays(int mode, int[] first, int first_offset,
	    int[] count, int count_offset, int drawcount) {
	try{delegate.glMultiDrawArrays(mode, first, first_offset, count,
		count_offset, drawcount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param indirect
     * @param primcount
     * @param stride
     * @see javax.media.opengl.GL2GL3#glMultiDrawArraysIndirectAMD(int, java.nio.Buffer, int, int)
     */
    public void glMultiDrawArraysIndirectAMD(int mode, Buffer indirect,
	    int primcount, int stride) {
	try{delegate.glMultiDrawArraysIndirectAMD(mode, indirect, primcount, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param renderbuffers
     * @see javax.media.opengl.GL#glDeleteRenderbuffers(int, java.nio.IntBuffer)
     */
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
	try{delegate.glDeleteRenderbuffers(n, renderbuffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param size
     * @param type
     * @param stride
     * @param pointer_buffer_offset
     * @see javax.media.opengl.GL2ES3#glVertexAttribIPointer(int, int, int, int, long)
     */
    public void glVertexAttribIPointer(int index, int size, int type,
	    int stride, long pointer_buffer_offset) {
	try{delegate.glVertexAttribIPointer(index, size, type, stride,
		pointer_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param source
     * @param id
     * @param length
     * @param message
     * @see javax.media.opengl.GL2ES2#glPushDebugGroup(int, int, int, java.nio.ByteBuffer)
     */
    public void glPushDebugGroup(int source, int id, int length,
	    ByteBuffer message) {
	try{delegate.glPushDebugGroup(source, id, length, message);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @param drawcount
     * @see javax.media.opengl.GL2GL3#glMultiDrawElements(int, java.nio.IntBuffer, int, com.jogamp.common.nio.PointerBuffer, int)
     */
    public void glMultiDrawElements(int mode, IntBuffer count, int type,
	    PointerBuffer indices, int drawcount) {
	try{delegate.glMultiDrawElements(mode, count, type, indices, drawcount);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param renderbuffers
     * @param renderbuffers_offset
     * @see javax.media.opengl.GL#glDeleteRenderbuffers(int, int[], int)
     */
    public void glDeleteRenderbuffers(int n, int[] renderbuffers,
	    int renderbuffers_offset) {
	try{delegate.glDeleteRenderbuffers(n, renderbuffers, renderbuffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param source
     * @param id
     * @param length
     * @param message
     * @param message_offset
     * @see javax.media.opengl.GL2ES2#glPushDebugGroup(int, int, int, byte[], int)
     */
    public void glPushDebugGroup(int source, int id, int length,
	    byte[] message, int message_offset) {
	try{delegate.glPushDebugGroup(source, id, length, message, message_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GL2ES3#glIsPBOPackEnabled()
     */
    public boolean glIsPBOPackEnabled() {
	try{ return delegate.glIsPBOPackEnabled();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param count
     * @param string
     * @param length
     * @see javax.media.opengl.GL2ES2#glShaderSource(int, int, java.lang.String[], java.nio.IntBuffer)
     */
    public void glShaderSource(int shader, int count, String[] string,
	    IntBuffer length) {
	try{delegate.glShaderSource(shader, count, string, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GL2ES3#glIsPBOUnpackEnabled()
     */
    public boolean glIsPBOUnpackEnabled() {
	try{ return delegate.glIsPBOUnpackEnabled();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param textures
     * @see javax.media.opengl.GL#glDeleteTextures(int, java.nio.IntBuffer)
     */
    public void glDeleteTextures(int n, IntBuffer textures) {
	try{delegate.glDeleteTextures(n, textures);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param type
     * @param indirect
     * @param primcount
     * @param stride
     * @see javax.media.opengl.GL2GL3#glMultiDrawElementsIndirectAMD(int, int, java.nio.Buffer, int, int)
     */
    public void glMultiDrawElementsIndirectAMD(int mode, int type,
	    Buffer indirect, int primcount, int stride) {
	try{delegate.glMultiDrawElementsIndirectAMD(mode, type, indirect,
		primcount, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param textures
     * @param textures_offset
     * @see javax.media.opengl.GL#glDeleteTextures(int, int[], int)
     */
    public void glDeleteTextures(int n, int[] textures, int textures_offset) {
	try{delegate.glDeleteTextures(n, textures, textures_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shader
     * @param count
     * @param string
     * @param length
     * @param length_offset
     * @see javax.media.opengl.GL2ES2#glShaderSource(int, int, java.lang.String[], int[], int)
     */
    public void glShaderSource(int shader, int count, String[] string,
	    int[] length, int length_offset) {
	try{delegate.glShaderSource(shader, count, string, length, length_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param framebuffer
     * @param pname
     * @param param
     * @see javax.media.opengl.GL2GL3#glNamedFramebufferParameteriEXT(int, int, int)
     */
    public void glNamedFramebufferParameteriEXT(int framebuffer, int pname,
	    int param) {
	try{delegate.glNamedFramebufferParameteriEXT(framebuffer, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param func
     * @see javax.media.opengl.GL#glDepthFunc(int)
     */
    public void glDepthFunc(int func) {
	try{delegate.glDepthFunc(func);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param face
     * @param func
     * @param ref
     * @param mask
     * @see javax.media.opengl.GL2ES2#glStencilFuncSeparate(int, int, int, int)
     */
    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
	try{delegate.glStencilFuncSeparate(face, func, ref, mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param namelen
     * @param name
     * @param stringlen
     * @param string
     * @see javax.media.opengl.GL2GL3#glNamedStringARB(int, int, java.lang.String, int, java.lang.String)
     */
    public void glNamedStringARB(int type, int namelen, String name,
	    int stringlen, String string) {
	try{delegate.glNamedStringARB(type, namelen, name, stringlen, string);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param flag
     * @see javax.media.opengl.GL#glDepthMask(boolean)
     */
    public void glDepthMask(boolean flag) {
	try{delegate.glDepthMask(flag);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param face
     * @param mask
     * @see javax.media.opengl.GL2ES2#glStencilMaskSeparate(int, int)
     */
    public void glStencilMaskSeparate(int face, int mask) {
	try{delegate.glStencilMaskSeparate(face, mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glNormalFormatNV(int, int)
     */
    public void glNormalFormatNV(int type, int stride) {
	try{delegate.glNormalFormatNV(type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param face
     * @param fail
     * @param zfail
     * @param zpass
     * @see javax.media.opengl.GL2ES2#glStencilOpSeparate(int, int, int, int)
     */
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
	try{delegate.glStencilOpSeparate(face, fail, zfail, zpass);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param param
     * @see javax.media.opengl.GL2GL3#glPixelStoref(int, float)
     */
    public void glPixelStoref(int pname, float param) {
	try{delegate.glPixelStoref(pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param cap
     * @see javax.media.opengl.GL#glDisable(int)
     */
    public void glDisable(int cap) {
	try{delegate.glDisable(cap);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param param
     * @see javax.media.opengl.GL2GL3#glPointParameterf(int, float)
     */
    public void glPointParameterf(int pname, float param) {
	try{delegate.glPointParameterf(pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param border
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL2ES2#glTexImage3D(int, int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexImage3D(int target, int level, int internalformat,
	    int width, int height, int depth, int border, int format, int type,
	    Buffer pixels) {
	try{delegate.glTexImage3D(target, level, internalformat, width, height,
		depth, border, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param first
     * @param count
     * @see javax.media.opengl.GL#glDrawArrays(int, int, int)
     */
    public void glDrawArrays(int mode, int first, int count) {
	try{delegate.glDrawArrays(mode, first, count);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glPointParameterfv(int, java.nio.FloatBuffer)
     */
    public void glPointParameterfv(int pname, FloatBuffer params) {
	try{delegate.glPointParameterfv(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices
     * @see javax.media.opengl.GL#glDrawElements(int, int, int, java.nio.Buffer)
     */
    public void glDrawElements(int mode, int count, int type, Buffer indices) {
	try{delegate.glDrawElements(mode, count, type, indices);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @param border
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL2ES2#glTexImage3D(int, int, int, int, int, int, int, int, int, long)
     */
    public void glTexImage3D(int target, int level, int internalformat,
	    int width, int height, int depth, int border, int format, int type,
	    long pixels_buffer_offset) {
	try{delegate.glTexImage3D(target, level, internalformat, width, height,
		depth, border, format, type, pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glPointParameterfv(int, float[], int)
     */
    public void glPointParameterfv(int pname, float[] params, int params_offset) {
	try{delegate.glPointParameterfv(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param param
     * @see javax.media.opengl.GL2GL3#glPointParameteri(int, int)
     */
    public void glPointParameteri(int pname, int param) {
	try{delegate.glPointParameteri(pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @param count
     * @param type
     * @param indices_buffer_offset
     * @see javax.media.opengl.GL#glDrawElements(int, int, int, long)
     */
    public void glDrawElements(int mode, int count, int type,
	    long indices_buffer_offset) {
	try{delegate.glDrawElements(mode, count, type, indices_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glPointParameteriv(int, java.nio.IntBuffer)
     */
    public void glPointParameteriv(int pname, IntBuffer params) {
	try{delegate.glPointParameteriv(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param width
     * @param height
     * @param depth
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL2ES2#glTexSubImage3D(int, int, int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexSubImage3D(int target, int level, int xoffset,
	    int yoffset, int zoffset, int width, int height, int depth,
	    int format, int type, Buffer pixels) {
	try{delegate.glTexSubImage3D(target, level, xoffset, yoffset, zoffset,
		width, height, depth, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param cap
     * @see javax.media.opengl.GL#glEnable(int)
     */
    public void glEnable(int cap) {
	try{delegate.glEnable(cap);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glPointParameteriv(int, int[], int)
     */
    public void glPointParameteriv(int pname, int[] params, int params_offset) {
	try{delegate.glPointParameteriv(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL#glFinish()
     */
    public void glFinish() {
	try{delegate.glFinish();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param size
     * @see javax.media.opengl.GL2GL3#glPointSize(float)
     */
    public void glPointSize(float size) {
	try{delegate.glPointSize(size);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL#glFlush()
     */
    public void glFlush() {
	try{delegate.glFlush();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param zoffset
     * @param width
     * @param height
     * @param depth
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL2ES2#glTexSubImage3D(int, int, int, int, int, int, int, int, int, int, long)
     */
    public void glTexSubImage3D(int target, int level, int xoffset,
	    int yoffset, int zoffset, int width, int height, int depth,
	    int format, int type, long pixels_buffer_offset) {
	try{delegate.glTexSubImage3D(target, level, xoffset, yoffset, zoffset,
		width, height, depth, format, type, pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param face
     * @param mode
     * @see javax.media.opengl.GL2GL3#glPolygonMode(int, int)
     */
    public void glPolygonMode(int face, int mode) {
	try{delegate.glPolygonMode(face, mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param offset
     * @param length
     * @see javax.media.opengl.GL#glFlushMappedBufferRange(int, long, long)
     */
    public void glFlushMappedBufferRange(int target, long offset, long length) {
	try{delegate.glFlushMappedBufferRange(target, offset, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @see javax.media.opengl.GL2GL3#glPrimitiveRestartIndex(int)
     */
    public void glPrimitiveRestartIndex(int index) {
	try{delegate.glPrimitiveRestartIndex(index);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @see javax.media.opengl.GL2ES2#glUniform1f(int, float)
     */
    public void glUniform1f(int location, float x) {
	try{delegate.glUniform1f(location, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param pname
     * @param value
     * @see javax.media.opengl.GL2GL3#glProgramParameteriARB(int, int, int)
     */
    public void glProgramParameteriARB(int program, int pname, int value) {
	try{delegate.glProgramParameteriARB(program, pname, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param renderbuffertarget
     * @param renderbuffer
     * @see javax.media.opengl.GL#glFramebufferRenderbuffer(int, int, int, int)
     */
    public void glFramebufferRenderbuffer(int target, int attachment,
	    int renderbuffertarget, int renderbuffer) {
	try{delegate.glFramebufferRenderbuffer(target, attachment,
		renderbuffertarget, renderbuffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform1fv(int, int, java.nio.FloatBuffer)
     */
    public void glUniform1fv(int location, int count, FloatBuffer v) {
	try{delegate.glUniform1fv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param value
     * @see javax.media.opengl.GL2GL3#glProgramUniformui64NV(int, int, long)
     */
    public void glProgramUniformui64NV(int program, int location, long value) {
	try{delegate.glProgramUniformui64NV(program, location, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param textarget
     * @param texture
     * @param level
     * @see javax.media.opengl.GL#glFramebufferTexture2D(int, int, int, int, int)
     */
    public void glFramebufferTexture2D(int target, int attachment,
	    int textarget, int texture, int level) {
	try{delegate.glFramebufferTexture2D(target, attachment, textarget, texture,
		level);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2GL3#glProgramUniformui64vNV(int, int, int, java.nio.LongBuffer)
     */
    public void glProgramUniformui64vNV(int program, int location, int count,
	    LongBuffer value) {
	try{delegate.glProgramUniformui64vNV(program, location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform1fv(int, int, float[], int)
     */
    public void glUniform1fv(int location, int count, float[] v, int v_offset) {
	try{delegate.glUniform1fv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @see javax.media.opengl.GL2ES2#glUniform1i(int, int)
     */
    public void glUniform1i(int location, int x) {
	try{delegate.glUniform1i(location, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2GL3#glProgramUniformui64vNV(int, int, int, long[], int)
     */
    public void glProgramUniformui64vNV(int program, int location, int count,
	    long[] value, int value_offset) {
	try{delegate.glProgramUniformui64vNV(program, location, count, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL#glFrontFace(int)
     */
    public void glFrontFace(int mode) {
	try{delegate.glFrontFace(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform1iv(int, int, java.nio.IntBuffer)
     */
    public void glUniform1iv(int location, int count, IntBuffer v) {
	try{delegate.glUniform1iv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param buffers
     * @see javax.media.opengl.GL#glGenBuffers(int, java.nio.IntBuffer)
     */
    public void glGenBuffers(int n, IntBuffer buffers) {
	try{delegate.glGenBuffers(n, buffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param size
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glSecondaryColorFormatNV(int, int, int)
     */
    public void glSecondaryColorFormatNV(int size, int type, int stride) {
	try{delegate.glSecondaryColorFormatNV(size, type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform1iv(int, int, int[], int)
     */
    public void glUniform1iv(int location, int count, int[] v, int v_offset) {
	try{delegate.glUniform1iv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param buffers
     * @param buffers_offset
     * @see javax.media.opengl.GL#glGenBuffers(int, int[], int)
     */
    public void glGenBuffers(int n, int[] buffers, int buffers_offset) {
	try{delegate.glGenBuffers(n, buffers, buffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param index
     * @param val
     * @see javax.media.opengl.GL2GL3#glSetMultisamplefvAMD(int, int, java.nio.FloatBuffer)
     */
    public void glSetMultisamplefvAMD(int pname, int index, FloatBuffer val) {
	try{delegate.glSetMultisamplefvAMD(pname, index, val);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @see javax.media.opengl.GL2ES2#glUniform2f(int, float, float)
     */
    public void glUniform2f(int location, float x, float y) {
	try{delegate.glUniform2f(location, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param framebuffers
     * @see javax.media.opengl.GL#glGenFramebuffers(int, java.nio.IntBuffer)
     */
    public void glGenFramebuffers(int n, IntBuffer framebuffers) {
	try{delegate.glGenFramebuffers(n, framebuffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param index
     * @param val
     * @param val_offset
     * @see javax.media.opengl.GL2GL3#glSetMultisamplefvAMD(int, int, float[], int)
     */
    public void glSetMultisamplefvAMD(int pname, int index, float[] val,
	    int val_offset) {
	try{delegate.glSetMultisamplefvAMD(pname, index, val, val_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform2fv(int, int, java.nio.FloatBuffer)
     */
    public void glUniform2fv(int location, int count, FloatBuffer v) {
	try{delegate.glUniform2fv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param face
     * @param value
     * @see javax.media.opengl.GL2GL3#glStencilOpValueAMD(int, int)
     */
    public void glStencilOpValueAMD(int face, int value) {
	try{delegate.glStencilOpValueAMD(face, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param framebuffers
     * @param framebuffers_offset
     * @see javax.media.opengl.GL#glGenFramebuffers(int, int[], int)
     */
    public void glGenFramebuffers(int n, int[] framebuffers,
	    int framebuffers_offset) {
	try{delegate.glGenFramebuffers(n, framebuffers, framebuffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform2fv(int, int, float[], int)
     */
    public void glUniform2fv(int location, int count, float[] v, int v_offset) {
	try{delegate.glUniform2fv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param factor
     * @see javax.media.opengl.GL2GL3#glTessellationFactorAMD(float)
     */
    public void glTessellationFactorAMD(float factor) {
	try{delegate.glTessellationFactorAMD(factor);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @see javax.media.opengl.GL2ES2#glUniform2i(int, int, int)
     */
    public void glUniform2i(int location, int x, int y) {
	try{delegate.glUniform2i(location, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param renderbuffers
     * @see javax.media.opengl.GL#glGenRenderbuffers(int, java.nio.IntBuffer)
     */
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
	try{delegate.glGenRenderbuffers(n, renderbuffers);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mode
     * @see javax.media.opengl.GL2GL3#glTessellationModeAMD(int)
     */
    public void glTessellationModeAMD(int mode) {
	try{delegate.glTessellationModeAMD(mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform2iv(int, int, java.nio.IntBuffer)
     */
    public void glUniform2iv(int location, int count, IntBuffer v) {
	try{delegate.glUniform2iv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param internalformat
     * @param buffer
     * @see javax.media.opengl.GL2GL3#glTexBuffer(int, int, int)
     */
    public void glTexBuffer(int target, int internalformat, int buffer) {
	try{delegate.glTexBuffer(target, internalformat, buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param renderbuffers
     * @param renderbuffers_offset
     * @see javax.media.opengl.GL#glGenRenderbuffers(int, int[], int)
     */
    public void glGenRenderbuffers(int n, int[] renderbuffers,
	    int renderbuffers_offset) {
	try{delegate.glGenRenderbuffers(n, renderbuffers, renderbuffers_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform2iv(int, int, int[], int)
     */
    public void glUniform2iv(int location, int count, int[] v, int v_offset) {
	try{delegate.glUniform2iv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param size
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glTexCoordFormatNV(int, int, int)
     */
    public void glTexCoordFormatNV(int size, int type, int stride) {
	try{delegate.glTexCoordFormatNV(size, type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param textures
     * @see javax.media.opengl.GL#glGenTextures(int, java.nio.IntBuffer)
     */
    public void glGenTextures(int n, IntBuffer textures) {
	try{delegate.glGenTextures(n, textures);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2ES2#glUniform3f(int, float, float, float)
     */
    public void glUniform3f(int location, float x, float y, float z) {
	try{delegate.glUniform3f(location, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalFormat
     * @param width
     * @param border
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL2GL3#glTexImage1D(int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexImage1D(int target, int level, int internalFormat,
	    int width, int border, int format, int type, Buffer pixels) {
	try{delegate.glTexImage1D(target, level, internalFormat, width, border,
		format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform3fv(int, int, java.nio.FloatBuffer)
     */
    public void glUniform3fv(int location, int count, FloatBuffer v) {
	try{delegate.glUniform3fv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param textures
     * @param textures_offset
     * @see javax.media.opengl.GL#glGenTextures(int, int[], int)
     */
    public void glGenTextures(int n, int[] textures, int textures_offset) {
	try{delegate.glGenTextures(n, textures, textures_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalFormat
     * @param width
     * @param border
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL2GL3#glTexImage1D(int, int, int, int, int, int, int, long)
     */
    public void glTexImage1D(int target, int level, int internalFormat,
	    int width, int border, int format, int type,
	    long pixels_buffer_offset) {
	try{delegate.glTexImage1D(target, level, internalFormat, width, border,
		format, type, pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @see javax.media.opengl.GL#glGenerateMipmap(int)
     */
    public void glGenerateMipmap(int target) {
	try{delegate.glGenerateMipmap(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform3fv(int, int, float[], int)
     */
    public void glUniform3fv(int location, int count, float[] v, int v_offset) {
	try{delegate.glUniform3fv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param coverageSamples
     * @param colorSamples
     * @param internalFormat
     * @param width
     * @param height
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTexImage2DMultisampleCoverageNV(int, int, int, int, int, int, boolean)
     */
    public void glTexImage2DMultisampleCoverageNV(int target,
	    int coverageSamples, int colorSamples, int internalFormat,
	    int width, int height, boolean fixedSampleLocations) {
	try{delegate.glTexImage2DMultisampleCoverageNV(target, coverageSamples,
		colorSamples, internalFormat, width, height,
		fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetBooleanv(int, java.nio.ByteBuffer)
     */
    public void glGetBooleanv(int pname, ByteBuffer params) {
	try{delegate.glGetBooleanv(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2ES2#glUniform3i(int, int, int, int)
     */
    public void glUniform3i(int location, int x, int y, int z) {
	try{delegate.glUniform3i(location, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform3iv(int, int, java.nio.IntBuffer)
     */
    public void glUniform3iv(int location, int count, IntBuffer v) {
	try{delegate.glUniform3iv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetBooleanv(int, byte[], int)
     */
    public void glGetBooleanv(int pname, byte[] params, int params_offset) {
	try{delegate.glGetBooleanv(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param coverageSamples
     * @param colorSamples
     * @param internalFormat
     * @param width
     * @param height
     * @param depth
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTexImage3DMultisampleCoverageNV(int, int, int, int, int, int, int, boolean)
     */
    public void glTexImage3DMultisampleCoverageNV(int target,
	    int coverageSamples, int colorSamples, int internalFormat,
	    int width, int height, int depth, boolean fixedSampleLocations) {
	try{delegate.glTexImage3DMultisampleCoverageNV(target, coverageSamples,
		colorSamples, internalFormat, width, height, depth,
		fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform3iv(int, int, int[], int)
     */
    public void glUniform3iv(int location, int count, int[] v, int v_offset) {
	try{delegate.glUniform3iv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetBufferParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
	try{delegate.glGetBufferParameteriv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2ES2#glUniform4f(int, float, float, float, float)
     */
    public void glUniform4f(int location, float x, float y, float z, float w) {
	try{delegate.glUniform4f(location, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glTexParameterIiv(int, int, java.nio.IntBuffer)
     */
    public void glTexParameterIiv(int target, int pname, IntBuffer params) {
	try{delegate.glTexParameterIiv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetBufferParameteriv(int, int, int[], int)
     */
    public void glGetBufferParameteriv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetBufferParameteriv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform4fv(int, int, java.nio.FloatBuffer)
     */
    public void glUniform4fv(int location, int count, FloatBuffer v) {
	try{delegate.glUniform4fv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glTexParameterIiv(int, int, int[], int)
     */
    public void glTexParameterIiv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glTexParameterIiv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GL#glGetError()
     */
    public int glGetError() {
	try{ return delegate.glGetError();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform4fv(int, int, float[], int)
     */
    public void glUniform4fv(int location, int count, float[] v, int v_offset) {
	try{delegate.glUniform4fv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetFloatv(int, java.nio.FloatBuffer)
     */
    public void glGetFloatv(int pname, FloatBuffer params) {
	try{delegate.glGetFloatv(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL2GL3#glTexParameterIuiv(int, int, java.nio.IntBuffer)
     */
    public void glTexParameterIuiv(int target, int pname, IntBuffer params) {
	try{delegate.glTexParameterIuiv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2ES2#glUniform4i(int, int, int, int, int)
     */
    public void glUniform4i(int location, int x, int y, int z, int w) {
	try{delegate.glUniform4i(location, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetFloatv(int, float[], int)
     */
    public void glGetFloatv(int pname, float[] params, int params_offset) {
	try{delegate.glGetFloatv(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL2GL3#glTexParameterIuiv(int, int, int[], int)
     */
    public void glTexParameterIuiv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glTexParameterIuiv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @see javax.media.opengl.GL2ES2#glUniform4iv(int, int, java.nio.IntBuffer)
     */
    public void glUniform4iv(int location, int count, IntBuffer v) {
	try{delegate.glUniform4iv(location, count, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetFramebufferAttachmentParameteriv(int, int, int, java.nio.IntBuffer)
     */
    public void glGetFramebufferAttachmentParameteriv(int target,
	    int attachment, int pname, IntBuffer params) {
	try{delegate.glGetFramebufferAttachmentParameteriv(target, attachment,
		pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param width
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL2GL3#glTexSubImage1D(int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexSubImage1D(int target, int level, int xoffset, int width,
	    int format, int type, Buffer pixels) {
	try{delegate.glTexSubImage1D(target, level, xoffset, width, format, type,
		pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2ES2#glUniform4iv(int, int, int[], int)
     */
    public void glUniform4iv(int location, int count, int[] v, int v_offset) {
	try{delegate.glUniform4iv(location, count, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES2#glUniformMatrix2fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix2fv(int location, int count, boolean transpose,
	    FloatBuffer value) {
	try{delegate.glUniformMatrix2fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param width
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL2GL3#glTexSubImage1D(int, int, int, int, int, int, long)
     */
    public void glTexSubImage1D(int target, int level, int xoffset, int width,
	    int format, int type, long pixels_buffer_offset) {
	try{delegate.glTexSubImage1D(target, level, xoffset, width, format, type,
		pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param attachment
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetFramebufferAttachmentParameteriv(int, int, int, int[], int)
     */
    public void glGetFramebufferAttachmentParameteriv(int target,
	    int attachment, int pname, int[] params, int params_offset) {
	try{delegate.glGetFramebufferAttachmentParameteriv(target, attachment,
		pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param internalformat
     * @param buffer
     * @param offset
     * @param size
     * @see javax.media.opengl.GL2GL3#glTextureBufferRangeEXT(int, int, int, int, long, long)
     */
    public void glTextureBufferRangeEXT(int texture, int target,
	    int internalformat, int buffer, long offset, long size) {
	try{delegate.glTextureBufferRangeEXT(texture, target, internalformat,
		buffer, offset, size);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES2#glUniformMatrix2fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix2fv(int location, int count, boolean transpose,
	    float[] value, int value_offset) {
	try{delegate.glUniformMatrix2fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @return
     * @see javax.media.opengl.GL#glGetGraphicsResetStatus()
     */
    public int glGetGraphicsResetStatus() {
	try{ return delegate.glGetGraphicsResetStatus();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetIntegerv(int, java.nio.IntBuffer)
     */
    public void glGetIntegerv(int pname, IntBuffer params) {
	try{delegate.glGetIntegerv(pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param coverageSamples
     * @param colorSamples
     * @param internalFormat
     * @param width
     * @param height
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTextureImage2DMultisampleCoverageNV(int, int, int, int, int, int, int, boolean)
     */
    public void glTextureImage2DMultisampleCoverageNV(int texture, int target,
	    int coverageSamples, int colorSamples, int internalFormat,
	    int width, int height, boolean fixedSampleLocations) {
	try{delegate.glTextureImage2DMultisampleCoverageNV(texture, target,
		coverageSamples, colorSamples, internalFormat, width, height,
		fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES2#glUniformMatrix3fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix3fv(int location, int count, boolean transpose,
	    FloatBuffer value) {
	try{delegate.glUniformMatrix3fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetIntegerv(int, int[], int)
     */
    public void glGetIntegerv(int pname, int[] params, int params_offset) {
	try{delegate.glGetIntegerv(pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES2#glUniformMatrix3fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix3fv(int location, int count, boolean transpose,
	    float[] value, int value_offset) {
	try{delegate.glUniformMatrix3fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param samples
     * @param internalFormat
     * @param width
     * @param height
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTextureImage2DMultisampleNV(int, int, int, int, int, int, boolean)
     */
    public void glTextureImage2DMultisampleNV(int texture, int target,
	    int samples, int internalFormat, int width, int height,
	    boolean fixedSampleLocations) {
	try{delegate.glTextureImage2DMultisampleNV(texture, target, samples,
		internalFormat, width, height, fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetRenderbufferParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glGetRenderbufferParameteriv(int target, int pname,
	    IntBuffer params) {
	try{delegate.glGetRenderbufferParameteriv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @see javax.media.opengl.GL2ES2#glUniformMatrix4fv(int, int, boolean, java.nio.FloatBuffer)
     */
    public void glUniformMatrix4fv(int location, int count, boolean transpose,
	    FloatBuffer value) {
	try{delegate.glUniformMatrix4fv(location, count, transpose, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param coverageSamples
     * @param colorSamples
     * @param internalFormat
     * @param width
     * @param height
     * @param depth
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTextureImage3DMultisampleCoverageNV(int, int, int, int, int, int, int, int, boolean)
     */
    public void glTextureImage3DMultisampleCoverageNV(int texture, int target,
	    int coverageSamples, int colorSamples, int internalFormat,
	    int width, int height, int depth, boolean fixedSampleLocations) {
	try{delegate.glTextureImage3DMultisampleCoverageNV(texture, target,
		coverageSamples, colorSamples, internalFormat, width, height,
		depth, fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetRenderbufferParameteriv(int, int, int[], int)
     */
    public void glGetRenderbufferParameteriv(int target, int pname,
	    int[] params, int params_offset) {
	try{delegate.glGetRenderbufferParameteriv(target, pname, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param transpose
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2ES2#glUniformMatrix4fv(int, int, boolean, float[], int)
     */
    public void glUniformMatrix4fv(int location, int count, boolean transpose,
	    float[] value, int value_offset) {
	try{delegate.glUniformMatrix4fv(location, count, transpose, value,
		value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param name
     * @return
     * @see javax.media.opengl.GL#glGetString(int)
     */
    public String glGetString(int name) {
	try{ return delegate.glGetString(name);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param samples
     * @param internalFormat
     * @param width
     * @param height
     * @param depth
     * @param fixedSampleLocations
     * @see javax.media.opengl.GL2GL3#glTextureImage3DMultisampleNV(int, int, int, int, int, int, int, boolean)
     */
    public void glTextureImage3DMultisampleNV(int texture, int target,
	    int samples, int internalFormat, int width, int height, int depth,
	    boolean fixedSampleLocations) {
	try{delegate.glTextureImage3DMultisampleNV(texture, target, samples,
		internalFormat, width, height, depth, fixedSampleLocations);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @see javax.media.opengl.GL2ES2#glUseProgram(int)
     */
    public void glUseProgram(int program) {
	try{delegate.glUseProgram(program);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetTexParameterfv(int, int, java.nio.FloatBuffer)
     */
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
	try{delegate.glGetTexParameterfv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @see javax.media.opengl.GL2ES2#glValidateProgram(int)
     */
    public void glValidateProgram(int program) {
	try{delegate.glValidateProgram(program);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param value
     * @see javax.media.opengl.GL2GL3#glUniformui64NV(int, long)
     */
    public void glUniformui64NV(int location, long value) {
	try{delegate.glUniformui64NV(location, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param x
     * @see javax.media.opengl.GL2ES2#glVertexAttrib1f(int, float)
     */
    public void glVertexAttrib1f(int indx, float x) {
	try{delegate.glVertexAttrib1f(indx, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetTexParameterfv(int, int, float[], int)
     */
    public void glGetTexParameterfv(int target, int pname, float[] params,
	    int params_offset) {
	try{delegate.glGetTexParameterfv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @see javax.media.opengl.GL2GL3#glUniformui64vNV(int, int, java.nio.LongBuffer)
     */
    public void glUniformui64vNV(int location, int count, LongBuffer value) {
	try{delegate.glUniformui64vNV(location, count, value);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @see javax.media.opengl.GL2ES2#glVertexAttrib1fv(int, java.nio.FloatBuffer)
     */
    public void glVertexAttrib1fv(int indx, FloatBuffer values) {
	try{delegate.glVertexAttrib1fv(indx, values);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glGetTexParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
	try{delegate.glGetTexParameteriv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param location
     * @param count
     * @param value
     * @param value_offset
     * @see javax.media.opengl.GL2GL3#glUniformui64vNV(int, int, long[], int)
     */
    public void glUniformui64vNV(int location, int count, long[] value,
	    int value_offset) {
	try{delegate.glUniformui64vNV(location, count, value, value_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @param values_offset
     * @see javax.media.opengl.GL2ES2#glVertexAttrib1fv(int, float[], int)
     */
    public void glVertexAttrib1fv(int indx, float[] values, int values_offset) {
	try{delegate.glVertexAttrib1fv(indx, values, values_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param bindingindex
     * @param buffer
     * @param offset
     * @param stride
     * @see javax.media.opengl.GL2GL3#glVertexArrayBindVertexBufferEXT(int, int, int, long, int)
     */
    public void glVertexArrayBindVertexBufferEXT(int vaobj, int bindingindex,
	    int buffer, long offset, int stride) {
	try{delegate.glVertexArrayBindVertexBufferEXT(vaobj, bindingindex, buffer,
		offset, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param x
     * @param y
     * @see javax.media.opengl.GL2ES2#glVertexAttrib2f(int, float, float)
     */
    public void glVertexAttrib2f(int indx, float x, float y) {
	try{delegate.glVertexAttrib2f(indx, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetTexParameteriv(int, int, int[], int)
     */
    public void glGetTexParameteriv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glGetTexParameteriv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @see javax.media.opengl.GL2ES2#glVertexAttrib2fv(int, java.nio.FloatBuffer)
     */
    public void glVertexAttrib2fv(int indx, FloatBuffer values) {
	try{delegate.glVertexAttrib2fv(indx, values);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param attribindex
     * @param bindingindex
     * @see javax.media.opengl.GL2GL3#glVertexArrayVertexAttribBindingEXT(int, int, int)
     */
    public void glVertexArrayVertexAttribBindingEXT(int vaobj, int attribindex,
	    int bindingindex) {
	try{delegate.glVertexArrayVertexAttribBindingEXT(vaobj, attribindex,
		bindingindex);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @see javax.media.opengl.GL#glGetnUniformfv(int, int, int, java.nio.FloatBuffer)
     */
    public void glGetnUniformfv(int program, int location, int bufSize,
	    FloatBuffer params) {
	try{delegate.glGetnUniformfv(program, location, bufSize, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @param values_offset
     * @see javax.media.opengl.GL2ES2#glVertexAttrib2fv(int, float[], int)
     */
    public void glVertexAttrib2fv(int indx, float[] values, int values_offset) {
	try{delegate.glVertexAttrib2fv(indx, values, values_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param attribindex
     * @param size
     * @param type
     * @param normalized
     * @param relativeoffset
     * @see javax.media.opengl.GL2GL3#glVertexArrayVertexAttribFormatEXT(int, int, int, int, boolean, int)
     */
    public void glVertexArrayVertexAttribFormatEXT(int vaobj, int attribindex,
	    int size, int type, boolean normalized, int relativeoffset) {
	try{delegate.glVertexArrayVertexAttribFormatEXT(vaobj, attribindex, size,
		type, normalized, relativeoffset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetnUniformfv(int, int, int, float[], int)
     */
    public void glGetnUniformfv(int program, int location, int bufSize,
	    float[] params, int params_offset) {
	try{delegate.glGetnUniformfv(program, location, bufSize, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2ES2#glVertexAttrib3f(int, float, float, float)
     */
    public void glVertexAttrib3f(int indx, float x, float y, float z) {
	try{delegate.glVertexAttrib3f(indx, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param attribindex
     * @param size
     * @param type
     * @param relativeoffset
     * @see javax.media.opengl.GL2GL3#glVertexArrayVertexAttribIFormatEXT(int, int, int, int, int)
     */
    public void glVertexArrayVertexAttribIFormatEXT(int vaobj, int attribindex,
	    int size, int type, int relativeoffset) {
	try{delegate.glVertexArrayVertexAttribIFormatEXT(vaobj, attribindex, size,
		type, relativeoffset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @see javax.media.opengl.GL#glGetnUniformiv(int, int, int, java.nio.IntBuffer)
     */
    public void glGetnUniformiv(int program, int location, int bufSize,
	    IntBuffer params) {
	try{delegate.glGetnUniformiv(program, location, bufSize, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @see javax.media.opengl.GL2ES2#glVertexAttrib3fv(int, java.nio.FloatBuffer)
     */
    public void glVertexAttrib3fv(int indx, FloatBuffer values) {
	try{delegate.glVertexAttrib3fv(indx, values);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param attribindex
     * @param size
     * @param type
     * @param relativeoffset
     * @see javax.media.opengl.GL2GL3#glVertexArrayVertexAttribLFormatEXT(int, int, int, int, int)
     */
    public void glVertexArrayVertexAttribLFormatEXT(int vaobj, int attribindex,
	    int size, int type, int relativeoffset) {
	try{delegate.glVertexArrayVertexAttribLFormatEXT(vaobj, attribindex, size,
		type, relativeoffset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param program
     * @param location
     * @param bufSize
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glGetnUniformiv(int, int, int, int[], int)
     */
    public void glGetnUniformiv(int program, int location, int bufSize,
	    int[] params, int params_offset) {
	try{delegate.glGetnUniformiv(program, location, bufSize, params,
		params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @param values_offset
     * @see javax.media.opengl.GL2ES2#glVertexAttrib3fv(int, float[], int)
     */
    public void glVertexAttrib3fv(int indx, float[] values, int values_offset) {
	try{delegate.glVertexAttrib3fv(indx, values, values_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2ES2#glVertexAttrib4f(int, float, float, float, float)
     */
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
	try{delegate.glVertexAttrib4f(indx, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param vaobj
     * @param bindingindex
     * @param divisor
     * @see javax.media.opengl.GL2GL3#glVertexArrayVertexBindingDivisorEXT(int, int, int)
     */
    public void glVertexArrayVertexBindingDivisorEXT(int vaobj,
	    int bindingindex, int divisor) {
	try{delegate.glVertexArrayVertexBindingDivisorEXT(vaobj, bindingindex,
		divisor);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param mode
     * @see javax.media.opengl.GL#glHint(int, int)
     */
    public void glHint(int target, int mode) {
	try{delegate.glHint(target, mode);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @see javax.media.opengl.GL2ES2#glVertexAttrib4fv(int, java.nio.FloatBuffer)
     */
    public void glVertexAttrib4fv(int indx, FloatBuffer values) {
	try{delegate.glVertexAttrib4fv(indx, values);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param buffer
     * @return
     * @see javax.media.opengl.GL#glIsBuffer(int)
     */
    public boolean glIsBuffer(int buffer) {
	try{ return delegate.glIsBuffer(buffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1d(int, double)
     */
    public void glVertexAttrib1d(int index, double x) {
	try{delegate.glVertexAttrib1d(index, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1dv(int, java.nio.DoubleBuffer)
     */
    public void glVertexAttrib1dv(int index, DoubleBuffer v) {
	try{delegate.glVertexAttrib1dv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param cap
     * @return
     * @see javax.media.opengl.GL#glIsEnabled(int)
     */
    public boolean glIsEnabled(int cap) {
	try{ return delegate.glIsEnabled(cap);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param values
     * @param values_offset
     * @see javax.media.opengl.GL2ES2#glVertexAttrib4fv(int, float[], int)
     */
    public void glVertexAttrib4fv(int indx, float[] values, int values_offset) {
	try{delegate.glVertexAttrib4fv(indx, values, values_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1dv(int, double[], int)
     */
    public void glVertexAttrib1dv(int index, double[] v, int v_offset) {
	try{delegate.glVertexAttrib1dv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param framebuffer
     * @return
     * @see javax.media.opengl.GL#glIsFramebuffer(int)
     */
    public boolean glIsFramebuffer(int framebuffer) {
	try{ return delegate.glIsFramebuffer(framebuffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param ptr
     * @see javax.media.opengl.GL2ES2#glVertexAttribPointer(int, int, int, boolean, int, java.nio.Buffer)
     */
    public void glVertexAttribPointer(int indx, int size, int type,
	    boolean normalized, int stride, Buffer ptr) {
	try{delegate.glVertexAttribPointer(indx, size, type, normalized, stride,
		ptr);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1s(int, short)
     */
    public void glVertexAttrib1s(int index, short x) {
	try{delegate.glVertexAttrib1s(index, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param renderbuffer
     * @return
     * @see javax.media.opengl.GL#glIsRenderbuffer(int)
     */
    public boolean glIsRenderbuffer(int renderbuffer) {
	try{ return delegate.glIsRenderbuffer(renderbuffer);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1sv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib1sv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib1sv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param indx
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @param ptr_buffer_offset
     * @see javax.media.opengl.GL2ES2#glVertexAttribPointer(int, int, int, boolean, int, long)
     */
    public void glVertexAttribPointer(int indx, int size, int type,
	    boolean normalized, int stride, long ptr_buffer_offset) {
	try{delegate.glVertexAttribPointer(indx, size, type, normalized, stride,
		ptr_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @return
     * @see javax.media.opengl.GL#glIsTexture(int)
     */
    public boolean glIsTexture(int texture) {
	try{ return delegate.glIsTexture(texture);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib1sv(int, short[], int)
     */
    public void glVertexAttrib1sv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib1sv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param width
     * @see javax.media.opengl.GL#glLineWidth(float)
     */
    public void glLineWidth(float width) {
	try{delegate.glLineWidth(width);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2d(int, double, double)
     */
    public void glVertexAttrib2d(int index, double x, double y) {
	try{delegate.glVertexAttrib2d(index, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * 
     * @see javax.media.opengl.GL2ES2#glReleaseShaderCompiler()
     */
    public void glReleaseShaderCompiler() {
	try{delegate.glReleaseShaderCompiler();} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param access
     * @return
     * @see javax.media.opengl.GL#glMapBuffer(int, int)
     */
    public ByteBuffer glMapBuffer(int target, int access) {
	try{ return delegate.glMapBuffer(target, access);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2dv(int, java.nio.DoubleBuffer)
     */
    public void glVertexAttrib2dv(int index, DoubleBuffer v) {
	try{delegate.glVertexAttrib2dv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param offset
     * @param length
     * @param access
     * @return
     * @see javax.media.opengl.GL#glMapBufferRange(int, long, long, int)
     */
    public ByteBuffer glMapBufferRange(int target, long offset, long length,
	    int access) {
	try{ return delegate.glMapBufferRange(target, offset, length, access);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2dv(int, double[], int)
     */
    public void glVertexAttrib2dv(int index, double[] v, int v_offset) {
	try{delegate.glVertexAttrib2dv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param shaders
     * @param binaryformat
     * @param binary
     * @param length
     * @see javax.media.opengl.GL2ES2#glShaderBinary(int, java.nio.IntBuffer, int, java.nio.Buffer, int)
     */
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat,
	    Buffer binary, int length) {
	try{delegate.glShaderBinary(n, shaders, binaryformat, binary, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2s(int, short, short)
     */
    public void glVertexAttrib2s(int index, short x, short y) {
	try{delegate.glVertexAttrib2s(index, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param pname
     * @param param
     * @see javax.media.opengl.GL#glPixelStorei(int, int)
     */
    public void glPixelStorei(int pname, int param) {
	try{delegate.glPixelStorei(pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2sv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib2sv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib2sv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param n
     * @param shaders
     * @param shaders_offset
     * @param binaryformat
     * @param binary
     * @param length
     * @see javax.media.opengl.GL2ES2#glShaderBinary(int, int[], int, int, java.nio.Buffer, int)
     */
    public void glShaderBinary(int n, int[] shaders, int shaders_offset,
	    int binaryformat, Buffer binary, int length) {
	try{delegate.glShaderBinary(n, shaders, shaders_offset, binaryformat,
		binary, length);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param factor
     * @param units
     * @see javax.media.opengl.GL#glPolygonOffset(float, float)
     */
    public void glPolygonOffset(float factor, float units) {
	try{delegate.glPolygonOffset(factor, units);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib2sv(int, short[], int)
     */
    public void glVertexAttrib2sv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib2sv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL#glReadPixels(int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glReadPixels(int x, int y, int width, int height, int format,
	    int type, Buffer pixels) {
	try{delegate.glReadPixels(x, y, width, height, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shadertype
     * @param precisiontype
     * @param range
     * @param precision
     * @see javax.media.opengl.GL2ES2#glGetShaderPrecisionFormat(int, int, java.nio.IntBuffer, java.nio.IntBuffer)
     */
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
	    IntBuffer range, IntBuffer precision) {
	try{delegate.glGetShaderPrecisionFormat(shadertype, precisiontype, range,
		precision);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3d(int, double, double, double)
     */
    public void glVertexAttrib3d(int index, double x, double y, double z) {
	try{delegate.glVertexAttrib3d(index, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3dv(int, java.nio.DoubleBuffer)
     */
    public void glVertexAttrib3dv(int index, DoubleBuffer v) {
	try{delegate.glVertexAttrib3dv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL#glReadPixels(int, int, int, int, int, int, long)
     */
    public void glReadPixels(int x, int y, int width, int height, int format,
	    int type, long pixels_buffer_offset) {
	try{delegate.glReadPixels(x, y, width, height, format, type,
		pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param shadertype
     * @param precisiontype
     * @param range
     * @param range_offset
     * @param precision
     * @param precision_offset
     * @see javax.media.opengl.GL2ES2#glGetShaderPrecisionFormat(int, int, int[], int, int[], int)
     */
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype,
	    int[] range, int range_offset, int[] precision, int precision_offset) {
	try{delegate.glGetShaderPrecisionFormat(shadertype, precisiontype, range,
		range_offset, precision, precision_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3dv(int, double[], int)
     */
    public void glVertexAttrib3dv(int index, double[] v, int v_offset) {
	try{delegate.glVertexAttrib3dv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param format
     * @param type
     * @param bufSize
     * @param data
     * @see javax.media.opengl.GL#glReadnPixels(int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glReadnPixels(int x, int y, int width, int height, int format,
	    int type, int bufSize, Buffer data) {
	try{delegate.glReadnPixels(x, y, width, height, format, type, bufSize, data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3s(int, short, short, short)
     */
    public void glVertexAttrib3s(int index, short x, short y, short z) {
	try{delegate.glVertexAttrib3s(index, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param zNear
     * @param zFar
     * @see javax.media.opengl.GL2ES2#glDepthRangef(float, float)
     */
    public void glDepthRangef(float zNear, float zFar) {
	try{delegate.glDepthRangef(zNear, zFar);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3sv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib3sv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib3sv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param internalformat
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glRenderbufferStorage(int, int, int, int)
     */
    public void glRenderbufferStorage(int target, int internalformat,
	    int width, int height) {
	try{delegate.glRenderbufferStorage(target, internalformat, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib3sv(int, short[], int)
     */
    public void glVertexAttrib3sv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib3sv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param zNear
     * @param zFar
     * @see javax.media.opengl.GL2ES2#glDepthRange(double, double)
     */
    public void glDepthRange(double zNear, double zFar) {
	try{delegate.glDepthRange(zNear, zFar);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param depth
     * @see javax.media.opengl.GL2ES2#glClearDepthf(float)
     */
    public void glClearDepthf(float depth) {
	try{delegate.glClearDepthf(depth);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nbv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttrib4Nbv(int index, ByteBuffer v) {
	try{delegate.glVertexAttrib4Nbv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param value
     * @param invert
     * @see javax.media.opengl.GL#glSampleCoverage(float, boolean)
     */
    public void glSampleCoverage(float value, boolean invert) {
	try{delegate.glSampleCoverage(value, invert);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param depth
     * @see javax.media.opengl.GL2ES2#glClearDepth(double)
     */
    public void glClearDepth(double depth) {
	try{delegate.glClearDepth(depth);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param array
     * @see javax.media.opengl.GL2ES2#glVertexAttribPointer(javax.media.opengl.GLArrayData)
     */
    public void glVertexAttribPointer(GLArrayData array) {
	try{delegate.glVertexAttribPointer(array);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nbv(int, byte[], int)
     */
    public void glVertexAttrib4Nbv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttrib4Nbv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param data
     * @see javax.media.opengl.GL2ES2#glUniform(javax.media.opengl.GLUniformData)
     */
    public void glUniform(GLUniformData data) {
	try{delegate.glUniform(data);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glScissor(int, int, int, int)
     */
    public void glScissor(int x, int y, int width, int height) {
	try{delegate.glScissor(x, y, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Niv(int, java.nio.IntBuffer)
     */
    public void glVertexAttrib4Niv(int index, IntBuffer v) {
	try{delegate.glVertexAttrib4Niv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param func
     * @param ref
     * @param mask
     * @see javax.media.opengl.GL#glStencilFunc(int, int, int)
     */
    public void glStencilFunc(int func, int ref, int mask) {
	try{delegate.glStencilFunc(func, ref, mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Niv(int, int[], int)
     */
    public void glVertexAttrib4Niv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttrib4Niv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param mask
     * @see javax.media.opengl.GL#glStencilMask(int)
     */
    public void glStencilMask(int mask) {
	try{delegate.glStencilMask(mask);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nsv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib4Nsv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib4Nsv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param fail
     * @param zfail
     * @param zpass
     * @see javax.media.opengl.GL#glStencilOp(int, int, int)
     */
    public void glStencilOp(int fail, int zfail, int zpass) {
	try{delegate.glStencilOp(fail, zfail, zpass);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nsv(int, short[], int)
     */
    public void glVertexAttrib4Nsv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib4Nsv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param border
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexImage2D(int target, int level, int internalformat,
	    int width, int height, int border, int format, int type,
	    Buffer pixels) {
	try{delegate.glTexImage2D(target, level, internalformat, width, height,
		border, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nub(int, byte, byte, byte, byte)
     */
    public void glVertexAttrib4Nub(int index, byte x, byte y, byte z, byte w) {
	try{delegate.glVertexAttrib4Nub(index, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nubv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttrib4Nubv(int index, ByteBuffer v) {
	try{delegate.glVertexAttrib4Nubv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param internalformat
     * @param width
     * @param height
     * @param border
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL#glTexImage2D(int, int, int, int, int, int, int, int, long)
     */
    public void glTexImage2D(int target, int level, int internalformat,
	    int width, int height, int border, int format, int type,
	    long pixels_buffer_offset) {
	try{delegate.glTexImage2D(target, level, internalformat, width, height,
		border, format, type, pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nubv(int, byte[], int)
     */
    public void glVertexAttrib4Nubv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttrib4Nubv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nuiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttrib4Nuiv(int index, IntBuffer v) {
	try{delegate.glVertexAttrib4Nuiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param param
     * @see javax.media.opengl.GL#glTexParameterf(int, int, float)
     */
    public void glTexParameterf(int target, int pname, float param) {
	try{delegate.glTexParameterf(target, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nuiv(int, int[], int)
     */
    public void glVertexAttrib4Nuiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttrib4Nuiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glTexParameterfv(int, int, java.nio.FloatBuffer)
     */
    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
	try{delegate.glTexParameterfv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nusv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib4Nusv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib4Nusv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glTexParameterfv(int, int, float[], int)
     */
    public void glTexParameterfv(int target, int pname, float[] params,
	    int params_offset) {
	try{delegate.glTexParameterfv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4Nusv(int, short[], int)
     */
    public void glVertexAttrib4Nusv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib4Nusv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4bv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttrib4bv(int index, ByteBuffer v) {
	try{delegate.glVertexAttrib4bv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param param
     * @see javax.media.opengl.GL#glTexParameteri(int, int, int)
     */
    public void glTexParameteri(int target, int pname, int param) {
	try{delegate.glTexParameteri(target, pname, param);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4bv(int, byte[], int)
     */
    public void glVertexAttrib4bv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttrib4bv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @see javax.media.opengl.GL#glTexParameteriv(int, int, java.nio.IntBuffer)
     */
    public void glTexParameteriv(int target, int pname, IntBuffer params) {
	try{delegate.glTexParameteriv(target, pname, params);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4d(int, double, double, double, double)
     */
    public void glVertexAttrib4d(int index, double x, double y, double z,
	    double w) {
	try{delegate.glVertexAttrib4d(index, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param pname
     * @param params
     * @param params_offset
     * @see javax.media.opengl.GL#glTexParameteriv(int, int, int[], int)
     */
    public void glTexParameteriv(int target, int pname, int[] params,
	    int params_offset) {
	try{delegate.glTexParameteriv(target, pname, params, params_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4dv(int, java.nio.DoubleBuffer)
     */
    public void glVertexAttrib4dv(int index, DoubleBuffer v) {
	try{delegate.glVertexAttrib4dv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @see javax.media.opengl.GL#glTexStorage1D(int, int, int, int)
     */
    public void glTexStorage1D(int target, int levels, int internalformat,
	    int width) {
	try{delegate.glTexStorage1D(target, levels, internalformat, width);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4dv(int, double[], int)
     */
    public void glVertexAttrib4dv(int index, double[] v, int v_offset) {
	try{delegate.glVertexAttrib4dv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4iv(int, java.nio.IntBuffer)
     */
    public void glVertexAttrib4iv(int index, IntBuffer v) {
	try{delegate.glVertexAttrib4iv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glTexStorage2D(int, int, int, int, int)
     */
    public void glTexStorage2D(int target, int levels, int internalformat,
	    int width, int height) {
	try{delegate.glTexStorage2D(target, levels, internalformat, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4iv(int, int[], int)
     */
    public void glVertexAttrib4iv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttrib4iv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @param w
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4s(int, short, short, short, short)
     */
    public void glVertexAttrib4s(int index, short x, short y, short z, short w) {
	try{delegate.glVertexAttrib4s(index, x, y, z, w);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @see javax.media.opengl.GL#glTexStorage3D(int, int, int, int, int, int)
     */
    public void glTexStorage3D(int target, int levels, int internalformat,
	    int width, int height, int depth) {
	try{delegate.glTexStorage3D(target, levels, internalformat, width, height,
		depth);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4sv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib4sv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib4sv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param width
     * @param height
     * @param format
     * @param type
     * @param pixels
     * @see javax.media.opengl.GL#glTexSubImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)
     */
    public void glTexSubImage2D(int target, int level, int xoffset,
	    int yoffset, int width, int height, int format, int type,
	    Buffer pixels) {
	try{delegate.glTexSubImage2D(target, level, xoffset, yoffset, width,
		height, format, type, pixels);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4sv(int, short[], int)
     */
    public void glVertexAttrib4sv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib4sv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4ubv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttrib4ubv(int index, ByteBuffer v) {
	try{delegate.glVertexAttrib4ubv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @param level
     * @param xoffset
     * @param yoffset
     * @param width
     * @param height
     * @param format
     * @param type
     * @param pixels_buffer_offset
     * @see javax.media.opengl.GL#glTexSubImage2D(int, int, int, int, int, int, int, int, long)
     */
    public void glTexSubImage2D(int target, int level, int xoffset,
	    int yoffset, int width, int height, int format, int type,
	    long pixels_buffer_offset) {
	try{delegate.glTexSubImage2D(target, level, xoffset, yoffset, width,
		height, format, type, pixels_buffer_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4ubv(int, byte[], int)
     */
    public void glVertexAttrib4ubv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttrib4ubv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4uiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttrib4uiv(int index, IntBuffer v) {
	try{delegate.glVertexAttrib4uiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @see javax.media.opengl.GL#glTextureStorage1D(int, int, int, int, int)
     */
    public void glTextureStorage1D(int texture, int target, int levels,
	    int internalformat, int width) {
	try{delegate.glTextureStorage1D(texture, target, levels, internalformat,
		width);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4uiv(int, int[], int)
     */
    public void glVertexAttrib4uiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttrib4uiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glTextureStorage2D(int, int, int, int, int, int)
     */
    public void glTextureStorage2D(int texture, int target, int levels,
	    int internalformat, int width, int height) {
	try{delegate.glTextureStorage2D(texture, target, levels, internalformat,
		width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4usv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttrib4usv(int index, ShortBuffer v) {
	try{delegate.glVertexAttrib4usv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttrib4usv(int, short[], int)
     */
    public void glVertexAttrib4usv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttrib4usv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param texture
     * @param target
     * @param levels
     * @param internalformat
     * @param width
     * @param height
     * @param depth
     * @see javax.media.opengl.GL#glTextureStorage3D(int, int, int, int, int, int, int)
     */
    public void glTextureStorage3D(int texture, int target, int levels,
	    int internalformat, int width, int height, int depth) {
	try{delegate.glTextureStorage3D(texture, target, levels, internalformat,
		width, height, depth);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param size
     * @param type
     * @param normalized
     * @param stride
     * @see javax.media.opengl.GL2GL3#glVertexAttribFormatNV(int, int, int, boolean, int)
     */
    public void glVertexAttribFormatNV(int index, int size, int type,
	    boolean normalized, int stride) {
	try{delegate.glVertexAttribFormatNV(index, size, type, normalized, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param target
     * @return
     * @see javax.media.opengl.GL#glUnmapBuffer(int)
     */
    public boolean glUnmapBuffer(int target) {
	try{ return delegate.glUnmapBuffer(target);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1i(int, int)
     */
    public void glVertexAttribI1i(int index, int x) {
	try{delegate.glVertexAttribI1i(index, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see javax.media.opengl.GL#glViewport(int, int, int, int)
     */
    public void glViewport(int x, int y, int width, int height) {
	try{delegate.glViewport(x, y, width, height);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1iv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI1iv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI1iv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1iv(int, int[], int)
     */
    public void glVertexAttribI1iv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI1iv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1ui(int, int)
     */
    public void glVertexAttribI1ui(int index, int x) {
	try{delegate.glVertexAttribI1ui(index, x);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1uiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI1uiv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI1uiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI1uiv(int, int[], int)
     */
    public void glVertexAttribI1uiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI1uiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2i(int, int, int)
     */
    public void glVertexAttribI2i(int index, int x, int y) {
	try{delegate.glVertexAttribI2i(index, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2iv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI2iv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI2iv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2iv(int, int[], int)
     */
    public void glVertexAttribI2iv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI2iv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2ui(int, int, int)
     */
    public void glVertexAttribI2ui(int index, int x, int y) {
	try{delegate.glVertexAttribI2ui(index, x, y);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2uiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI2uiv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI2uiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI2uiv(int, int[], int)
     */
    public void glVertexAttribI2uiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI2uiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3i(int, int, int, int)
     */
    public void glVertexAttribI3i(int index, int x, int y, int z) {
	try{delegate.glVertexAttribI3i(index, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3iv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI3iv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI3iv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3iv(int, int[], int)
     */
    public void glVertexAttribI3iv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI3iv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param x
     * @param y
     * @param z
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3ui(int, int, int, int)
     */
    public void glVertexAttribI3ui(int index, int x, int y, int z) {
	try{delegate.glVertexAttribI3ui(index, x, y, z);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3uiv(int, java.nio.IntBuffer)
     */
    public void glVertexAttribI3uiv(int index, IntBuffer v) {
	try{delegate.glVertexAttribI3uiv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI3uiv(int, int[], int)
     */
    public void glVertexAttribI3uiv(int index, int[] v, int v_offset) {
	try{delegate.glVertexAttribI3uiv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4bv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttribI4bv(int index, ByteBuffer v) {
	try{delegate.glVertexAttribI4bv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4bv(int, byte[], int)
     */
    public void glVertexAttribI4bv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttribI4bv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4sv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttribI4sv(int index, ShortBuffer v) {
	try{delegate.glVertexAttribI4sv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4sv(int, short[], int)
     */
    public void glVertexAttribI4sv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttribI4sv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4ubv(int, java.nio.ByteBuffer)
     */
    public void glVertexAttribI4ubv(int index, ByteBuffer v) {
	try{delegate.glVertexAttribI4ubv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4ubv(int, byte[], int)
     */
    public void glVertexAttribI4ubv(int index, byte[] v, int v_offset) {
	try{delegate.glVertexAttribI4ubv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4usv(int, java.nio.ShortBuffer)
     */
    public void glVertexAttribI4usv(int index, ShortBuffer v) {
	try{delegate.glVertexAttribI4usv(index, v);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param v
     * @param v_offset
     * @see javax.media.opengl.GL2GL3#glVertexAttribI4usv(int, short[], int)
     */
    public void glVertexAttribI4usv(int index, short[] v, int v_offset) {
	try{delegate.glVertexAttribI4usv(index, v, v_offset);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param index
     * @param size
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glVertexAttribIFormatNV(int, int, int, int)
     */
    public void glVertexAttribIFormatNV(int index, int size, int type,
	    int stride) {
	try{delegate.glVertexAttribIFormatNV(index, size, type, stride);} catch(GLException e){glException(e); throw e;}
    }
    /**
     * @param size
     * @param type
     * @param stride
     * @see javax.media.opengl.GL2GL3#glVertexFormatNV(int, int, int)
     */
    public void glVertexFormatNV(int size, int type, int stride) {
	try{delegate.glVertexFormatNV(size, type, stride);} catch(GLException e){glException(e); throw e;}
    }
}//end StateBeanBridgeGL3
