/*******************************************************************************
 * This file is part of the JAVA FILE DESCRIPTION TOOLKIT (JFDT)
 * A library for parsing files and mapping their data to/from java Beans.
 * ...which is now part of the JAVA TERMINAL REALITY FILE PARSERS project.
 * Copyright (c) 2012,2013 Chuck Ritola and any contributors to these files.
 * 
 *     JFDT is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     JDFT is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with jTRFP.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.jtrfp.jfdt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.jtrfp.jfdt.FailureBehavior;


/**
 * Utility class typically used from a ThirdPartyParseable's format description 'describeFormat()' method.<br>
 * Contains static methods to inform the parser on the structure of a file and how to map it to the properties of a bean.<br>
 * Also contains helper methods for reading and writing.
 * @author Chuck Ritola
 *
 */
public class Parser{
	public ParseMode parseMode;
	
	public EndianAwareDataInputStream is;
	public EndianAwareDataOutputStream os;
	
	private ByteOrder order=ByteOrder.BIG_ENDIAN;
	private Stack<ThirdPartyParseable> beanStack = new Stack<ThirdPartyParseable>();
	
	public boolean ignoreEOF=false;
	
	public ThirdPartyParseable peekBean(){return beanStack.peek();}
	public void popBean(){beanStack.pop();}
	
	public ByteOrder order(){return order;}
	
	public void pushBean(ThirdPartyParseable bean){
		beanStack.push(bean);
		}
	public void order(ByteOrder order){
		this.order=order;
		if(is!=null)is.setOrder(order);
		if(os!=null)os.setOrder(order);
		}
	
	/**
	 * Read the specified InputStream, parsing it and writing the property values to the given instantiated bean.<br>
	 *  When finished, the current position of the stream will be immediately after the data extracted.
	 * @param is						An InputStream supplying the raw data to be parsed.
	 * @param target					The bean to which the properties are to be updated. Parser is implicitly specified when passing this bean.
	 * @throws IllegalAccessException
	 * @throws UnrecognizedFormatException
	 * @since Sep 17, 2012
	 */
	public void readToExistingBean(InputStream is, ThirdPartyParseable target) 
			throws IllegalAccessException, UnrecognizedFormatException{
		readToExistingBean(new EndianAwareDataInputStream(new DataInputStream(is)),target);
		}//end readBean(...)
	
	/**
	 * Read the specified EndianAwareDataInputStream, parsing it and writing the property values to the given instantiated bean.<br>
	 *  When finished, the current position of the stream will be immediately after the data extracted.
	 * @param is						An endian-aware InputStream supplying the raw data to be parsed.
	 * @param target					The bean to which the properties are to be updated. Parser is implicitly specified when passing this bean.
	 * @throws IllegalAccessException
	 * @throws UnrecognizedFormatException
	 * @since Sep 17, 2012
	 */
	public void readToExistingBean(EndianAwareDataInputStream is, ThirdPartyParseable target) 
			throws IllegalAccessException, UnrecognizedFormatException{
		//target = (CLASS)Beans.instantiate(null, clazz.getName());
		ensureContextInstantiatedForReading(is,target);
		target.describeFormat(this);
		popBean();
		}//end readBean(...)
	
	/**
	 * Inform the Parser that all endian-sensitive data following the current position in the description is to be considered little-endian until otherwise specified.
	 * 
	 * @since Sep 17, 2012
	 */
	public void littleEndian(){
		order(ByteOrder.LITTLE_ENDIAN);
		}
	/**
	 * Inform the Parser that all endian-sensitive data following the current position in the description is to be considered big-endian until otherwise specified.
	 * 
	 * @since Sep 17, 2012
	 */
	public void bigEndian(){
		order(ByteOrder.BIG_ENDIAN);
		}
	
	
	/**
	 *  Read the specified InputStream, parsing it and writing its property values to a newly-created bean of the specified class.<br>
	 *  When finished, the current position of the stream will be immediately after the data extracted.
	 * @param is
	 * @param clazz							Class of the bean to which this data is to be parsed. Implied format description in that ThirdPartyParseable class.
	 * @return
	 * @throws IllegalAccessException
	 * @throws UnrecognizedFormatException
	 * @since Sep 17, 2012
	 */
	public <CLASS extends ThirdPartyParseable> CLASS readToNewBean(InputStream is, Class <CLASS> clazz) 
			throws IllegalAccessException, UnrecognizedFormatException{
		return readToNewBean(new EndianAwareDataInputStream(new DataInputStream(is)),clazz);
		}
	
	
	
	/**
	 *  Read the specified EndianAwareDataInputStream, parsing it and writing its property values to a newly-created bean of the specified class.<br>
	 *  When finished, the current position of the stream will be immediately after the data extracted.
	 * @param is
	 * @param clazz							Class of the bean to which this data is to be parsed. Implied format description in that ThirdPartyParseable class.
	 * @return
	 * @throws IllegalAccessException
	 * @throws UnrecognizedFormatException
	 * @since Sep 17, 2012
	 */
	public <CLASS extends ThirdPartyParseable> CLASS readToNewBean(EndianAwareDataInputStream is, Class <CLASS> clazz) 
			throws IllegalAccessException, UnrecognizedFormatException{
		CLASS result=null;
		try {
			result = (CLASS)clazz.newInstance();
			//result = (CLASS)Beans.instantiate(null, clazz.getName());
			ensureContextInstantiatedForReading(is,result);
			result.describeFormat(this);
			}
		catch(InstantiationException e)
			{e.printStackTrace();}
		finally
			{popBean();}
		return result;
		}//end readBean(...)
	
	
	/**
	 * Write ThirdPartyParseable bean to the given OutputStream.
	 * @param bean
	 * @param os
	 * @since Sep 17, 2012
	 */
	public void writeBean(ThirdPartyParseable bean,OutputStream os){
		writeBean(bean,new EndianAwareDataOutputStream(new DataOutputStream(os)));
		}
	
	/**
	 * Write ThirdPartyParseable bean to the given EndianAwareDataOutputStream.
	 * @param bean
	 * @param os
	 * @since Sep 17, 2012
	 */
	public void writeBean(ThirdPartyParseable bean, EndianAwareDataOutputStream os){
		if(bean==null)throw new NullPointerException();
		ensureContextInstantiatedForWriting(os,bean);
		try{bean.describeFormat(this);}
		catch(UnrecognizedFormatException e){e.printStackTrace();}//Shouldn't happen.
		popBean();
		}
	
	public String readUTF8FileToString(File f) throws FileNotFoundException, IOException{
		StringBuilder sb = new StringBuilder();
		FileInputStream fis = new FileInputStream(f);
		int value;
		while((value=fis.read())!=-1)
			{
			sb.append((char)value);
			}
		fis.close();
		return sb.toString();
		}//end readUTF8FileToString(...)
	
	private void ensureContextInstantiatedForReading(EndianAwareDataInputStream is, ThirdPartyParseable bean){
		//if(pc==null) pc= registerParseContext();
		os=null;this.is=is;
		pushBean(bean);
		//System.out.println("pushing bean: "+bean);
		parseMode=ParseMode.READ;
		}
	
	private void ensureContextInstantiatedForWriting(EndianAwareDataOutputStream os, ThirdPartyParseable bean){
		//if(pc==null) pc= registerParseContext();
		this.is=null; this.os=os;
		pushBean(bean);
		parseMode=ParseMode.WRITE;
		}
	
	////////// BEAN OPERATIONS ///////////////////////////////////////
	/*
	private static int getInt(ThirdPartyParseable obj, String property)
		{
		try{return (Integer)new PropertyDescriptor(property,obj.getClass()).getReadMethod().invoke(obj, null);}
		catch(Exception e){throw new RuntimeException(e);}
		}
	private static void setInt(ThirdPartyParseable obj, String property, Integer value)
		{
		try{new PropertyDescriptor(property,obj.getClass()).getWriteMethod().invoke(obj,value);}
		catch(Exception e){throw new RuntimeException(e);}
		}*/
	private <CLASS> CLASS get(ThirdPartyParseable obj, String property, Class <? extends CLASS> propertyReturnClass){
		//System.out.println(clazz.getName()+" object="+obj.getClass());
		try {
			Method meth = obj.getClass().getMethod("get"+Character.toUpperCase(property.charAt(0))+property.substring(1), null);
			if(propertyReturnClass==String.class){
				//Object result = new PropertyDescriptor(property,obj.getClass()).getReadMethod().invoke(obj, null);
				Object result = meth.invoke(obj, null);
				if(!result.getClass().isEnum())return (CLASS)new String(""+result);
				else return (CLASS)(((Enum)result).ordinal()+"");
				}
			return (CLASS)meth.invoke(obj, null);
			}
		catch(Exception e){throw new RuntimeException(e);}
		}
	
	private Class getPropertyReturnType(ThirdPartyParseable obj, String property) throws NoSuchMethodException
		{
		return obj.getClass().getMethod("get"+Character.toUpperCase(property.charAt(0))+property.substring(1), null).getReturnType();
		}
	
	private void set(ThirdPartyParseable obj, String property, Object value, Class <?> targetClass){
		if(value instanceof String){
			if(targetClass==Integer.class||targetClass==int.class)			{value=Integer.parseInt((String)value);}
			else if(targetClass==Double.class||targetClass==double.class)	{value=Double.parseDouble((String)value);}
			else if(targetClass==Float.class||targetClass==float.class)		{value=Float.parseFloat((String)value);}
			else if(targetClass.isEnum())										{value=targetClass.getEnumConstants()[Integer.parseInt((String)value)];}
			else if(targetClass==Byte.class||targetClass==byte.class)		{value=((String)value).getBytes();}
			else if(StringParser.class.isAssignableFrom(targetClass))
				{try{
					StringParser parser = (StringParser)targetClass.newInstance();
					value=parser.parseRead((String)value);
					}
				catch(IllegalAccessException e){e.printStackTrace();}
				catch(InstantiationException e){e.printStackTrace();}
				}//end StringParser
			}
		//else {throw new RuntimeException("Unrecognized property class: "+clazz.getName());}
		//System.out.println("Argument type: "+value.getClass().getSimpleName()+" bean type: "+obj.getClass()+" property name: "+property);
		//try{new PropertyDescriptor(property,obj.getClass()).getWriteMethod().invoke(obj,value);}
		invokeSet(obj,"set"+Character.toUpperCase(property.charAt(0))+property.substring(1),value,value.getClass());
		}
	
	private static void invokeSet(Object obj, String mName, Object value, Class argClass){
		if(argClass==Integer.class)argClass=int.class;
		if(argClass==Double.class) argClass=double.class;
		if(argClass==Boolean.class)argClass=boolean.class;
		if(argClass==Long.class)   argClass=long.class;
		try{obj.getClass().getMethod(mName, argClass).invoke(obj, value);}
		catch(NoSuchMethodException e){
			if(argClass==Object.class)
				throw new RuntimeException("Failed to find class for method "+ mName+" in "+obj.getClass().getName());
			argClass=argClass.getSuperclass();
			invokeSet(obj,mName,value,argClass);
			}
		catch(Exception e){e.printStackTrace();System.exit(0);}
		}
	
	/*private static String getString(ThirdPartyParseable obj, String property)
		{
		try{return (String)new PropertyDescriptor(property,obj.getClass()).getReadMethod().invoke(obj, null);}
		catch(Exception e){throw new RuntimeException(e);}
		}
	private static void setString(ThirdPartyParseable obj, String property, String value)
		{
		try{new PropertyDescriptor(property,obj.getClass()).getWriteMethod().invoke(obj,value);}
		catch(Exception e){throw new RuntimeException(e);}
		}
	public void indexedSetObject(ThirdPartyParseable obj, String property, int index, Object value)
		{
		try{new PropertyDescriptor(property,obj.getClass()).getWriteMethod().invoke(obj,index,value);}
		catch(Exception e){throw new RuntimeException(e);}
		}
	public Object indexedGetObject(ThirdPartyParseable obj, String property, int index)
		{
		try{return new PropertyDescriptor(property,obj.getClass()).getReadMethod().invoke(obj,index);}
		catch(Exception e){throw new RuntimeException(e);}
		}*/
	/**
	 * Used internally to track whether the parser is reading or writing a given file.
	 * @author Chuck Ritola
	 *
	 */
	public enum ParseMode
		{READ,WRITE}
	
	
	public void ignoreEOF(boolean doIt)
		{ignoreEOF=doIt;}
	
	/**
	 * Helper class for providing different behaviors depending on whether the Parser is reading or writing.
	 * @author Chuck Ritola
	 *
	 */
	protected abstract class RWHelper{
		public final void go(){
			try {
				switch(parseMode){
					case READ:
						{
						read(is,peekBean());
						break;
						}
					case WRITE:
						{
						write(os,peekBean());
						break;
						}
					}//end switch{}
				}//end try{}
			catch(Exception e){
				if(e instanceof EOFException && ignoreEOF)
					{}//Do nothing
				else if(e instanceof UnrecognizedFormatException)throw new UnrecognizedFormatException();
				else{
					e.printStackTrace();
					System.err.println("... this exception occured while accessing offset "+
					is.getReadTally()+" (0x"+Long.toHexString(is.getReadTally()).toUpperCase()+")");
					}
				}//end catch(...)
			}//end go()
		
		public abstract void read(EndianAwareDataInputStream is,  ThirdPartyParseable bean) throws IOException;
		public abstract void write(EndianAwareDataOutputStream os, ThirdPartyParseable bean) throws IOException;
		}//end RWHelper
	
	/////////// PARSE OPERATIONS ////////////////////////////////////
	
	/**
	 * Informs the Parser that the current position in the description contains a block of raw bytes of given length which should be mapped to this bean's property.
	 * @param count			Number of bytes to map from the current position in the description.
	 * @param dest			Bean property to which this data is to be mapped.
	 * @since Sep 17, 2012
	 */
	public void bytesOfCount(final int count, final PropertyDestination dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is,
						ThirdPartyParseable bean) throws IOException{
					byte [] data = new byte[count];
					if(count>1){
						is.readFully(data);
						dest.set(data, bean);
						}
					else if(count==1){
						is.readFully(data);
						dest.set(data[0], bean);
						}
					}//end read(...)

				@Override
				public void write(EndianAwareDataOutputStream os,
						ThirdPartyParseable bean) throws IOException{
					//byte [] data = get(bean, d,byte[].class);
					Object obj = dest.get(bean);
					if(obj instanceof byte[]){
						os.write((byte[])obj);
						}
					else{
						os.write(new byte[]{(Byte)obj});
						}
					}//end write(...)
			}.go();
		}//end bytesOfCount(...)
	
	/**
	 * Endian-aware mapping of a 8-byte IEEE 754 float (double) at the description's current position.
	 * @param dest
	 * @since Sep 18, 2012
	 */
	public void float8(final PropertyDestination<Double> dest)
		{
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is,
						ThirdPartyParseable bean) throws IOException{
					dest.set(is.readDouble(), bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os,
						ThirdPartyParseable bean) throws IOException{
					os.writeDouble(dest.get(bean));
					}
			}.go();
		}//end int4s
	
	/**
	 * Endian-aware mapping of a 4-byte IEEE 754 float at the description's current position.
	 * @param dest
	 * @since Sep 18, 2012
	 */
	public void float4(final PropertyDestination<Float> dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					dest.set(is.readFloat(), bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.writeFloat(dest.get(bean));
					}
			}.go();
		}//end int4s
	
	/**
	 * Endian-aware mapping of a 4-byte unsigned integer at the description's current position.
	 * @param dest			A long-based property
	 * @since Sep 17, 2012
	 */
	public void int4u(final PropertyDestination<Long> dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
				        long val = is.readInt();
				        if(val<0)val+=Integer.MAX_VALUE;
					dest.set(val, bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.writeInt(dest.get(bean).intValue());
					}
			}.go();
		}//end int4s
	
	/**
	 * Endian-aware mapping of a 8-byte signed integer (long) at the description's current position.
	 * @param dest			a long-based property
	 * @since Sep 18, 2012
	 */
	public void int8s(final PropertyDestination<Long> dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					dest.set(is.readLong(), bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.writeLong(dest.get(bean));
					}
			}.go();
		}//end int8s
	
	/**
	 * Endian-aware mapping of a 4-byte signed integer at the description's current position.
	 * @param dest
	 * @since Sep 17, 2012
	 */
	public void int4s(final PropertyDestination<Integer> dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					dest.set(is.readInt(), bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.writeInt(dest.get(bean));
					}
			}.go();
		}//end int4s
	
	/**
	 * Utility to flip the endian of the supplied set of bytes, returning a new set with the result and leaving the original alone.
	 * @param bytes
	 * @return
	 * @since Sep 17, 2012
	 */
	public byte []  flipEndian(final byte [] bytes){
		//Flip endian and try again.
		byte [] w = new byte[bytes.length];
		for(int i=0; i<bytes.length;i++)
			{w[i]=bytes[(bytes.length-1)-i];}
		return w;
		}
	
	/**
	 * Tell the parser to expect the given set of bytes, else resort to the supplied failure behavior.<br>
	 * <b>***ENDIAN-AWARE***</b> This method will flip the order of the expected bytes if the parser's order is LITTLE_ENDIAN.
	 * @param bytes				The bytes to expect in big-endian mode.
	 * @param failureBehavior	What to do if the expected bytes are not available
	 * @since Sep 17, 2012
	 */
	public void expectBytes(final byte [] bytes, final FailureBehavior failureBehavior){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					byte [] b = new byte[bytes.length];
					//System.out.println("b.length="+b.length+" pos="+is.getReadTally());
					is.mark(b.length);
					try{is.readFully(b);}
					catch(EOFException e){
					    if(failureBehavior == FailureBehavior.UNRECOGNIZED_FORMAT)
						throw new UnrecognizedFormatException();
					    else throw e;// No solution found.
					    // FLIP_ENDIAN isn't going to help here so it won't be considered.
					}//end catch(EOFException)
					if(order()==ByteOrder.LITTLE_ENDIAN){b=flipEndian(b);/*System.out.println("endian mode is little. Flipping input.");*/}
					/*
					System.out.print("Expected: ");
					for(byte thisByte:bytes)
						{
						System.out.print(Integer.toHexString((int)thisByte&0xFF)+" ");
						}
					System.out.print("\nGot: ");
					for(byte thisByte:b)
						{
						System.out.print(Integer.toHexString((int)thisByte&0xFF)+" ");
						}
					System.out.println();
					*/
					if(!Arrays.equals(b,bytes)){
						//System.out.println("No match. Position prior to resetting: "+is.getReadTally());
						is.reset();//reset, assuming the string is simply missing. //TODO: Smarter handling? Approximation?
						if(failureBehavior==FailureBehavior.UNRECOGNIZED_FORMAT){
							/*
							System.out.print("Expected: ");
							for(byte thisByte:bytes)
								{
								System.out.print(Integer.toHexString((int)thisByte&0xFF)+" ");
								}
							System.out.print("\nGot: ");
							for(byte thisByte:b)
								{
								System.out.print(Integer.toHexString((int)thisByte&0xFF)+" ");
								}
							System.out.println();
							*/
							throw new UnrecognizedFormatException();
							}//end if(throwException)
						else if(failureBehavior==FailureBehavior.FLIP_ENDIAN){
							byte [] w = flipEndian(bytes);
							expectBytes(w,FailureBehavior.UNRECOGNIZED_FORMAT);//Throws and escapes on fail
							//Success. Toggle endian.
							if(order()==ByteOrder.BIG_ENDIAN)order(ByteOrder.LITTLE_ENDIAN);
							else order(ByteOrder.BIG_ENDIAN);
							return;
							}
						}//end (!equal)
					//System.out.println("Expectation successful in class "+pc.beanStack.peek().getClass().getName()+" at offset 0x"+Long.toHexString(pc.is.getReadTally()));
					}//end read(...)

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					byte [] buf;
					buf=bytes;
					if(order()==ByteOrder.LITTLE_ENDIAN)buf=flipEndian(bytes);
					os.write(buf);
					}
			}.go();
		}//end expectBytes
	
	/**
	 * Expect the supplied String as ASCII string (1 byte=1 char), resorting to the supplied failureBehavior on failure.
	 * @param string
	 * @param failureBehavior
	 * @since Sep 17, 2012
	 */
	public void expectString(final String string, FailureBehavior failureBehavior){
		expectBytes(string.getBytes(),failureBehavior);
		}//end expectBytes
	
	/**
	 * Maps a block of raw bytes of unknown length, ending in a known patter, to a bean property.
	 * @param ending
	 * @param targetProperty
	 * @param includeEndingWhenReading
	 * @since Sep 17, 2012
	 */
	public void bytesEndingWith(final byte [] ending, final PropertyDestination targetProperty, final boolean includeEndingWhenReading){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is,
						 ThirdPartyParseable bean)
						throws IOException{
					ArrayList<Byte>bytes= new ArrayList<Byte>();
					try {
						if(ending==null)
							{//Read til EOF
							while(true){bytes.add(is.readByte());}
							}
						else{
							throw new RuntimeException("Feature not implemented yet: bytesEndingWith, using non-null ending.");
							//stringEndingWith(new String(ending),targetProperty,includeEndingWhenReading);
							}
						}
					catch(EOFException e){}
					byte [] result = new byte[bytes.size()];
					for(int i=0; i<result.length; i++)
						{result[i]=bytes.get(i);}
					targetProperty.set(result, bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os,
						 ThirdPartyParseable bean)
						throws IOException{
					os.write((byte [])targetProperty.get(bean));
					if(!includeEndingWhenReading&&ending!=null)os.write(ending);
					}
			
			}.go();
		}
	
	protected static <CLASS> CLASS convertFromString(String s,Class<CLASS>targetClass) throws NumberFormatException{
		Object result=null;

		if(targetClass==Integer.class||targetClass==int.class){result=Integer.parseInt(s);}
		else if(targetClass==Double.class||targetClass==double.class){result=Double.parseDouble(s);}
		else if(targetClass==Float.class||targetClass==float.class){result=Float.parseFloat(s);}
		else if(targetClass.isEnum()){int i=Integer.parseInt(s);while(i<0){i+=targetClass.getEnumConstants().length;}result=targetClass.getEnumConstants()[i];}
		else if(targetClass==Byte[].class||targetClass==byte[].class){result=(s).getBytes();}
		else if(targetClass==boolean.class||targetClass==Boolean.class){result=s.contentEquals("0")?false:true;}
		else {result=s;}
		return (CLASS)result;
		}
	
	/**
	 * A type describing a bean property. This is typically used in mapping pieces of a parsed file to the properties of a bean.
	 * @author Chuck Ritola
	 *
	 * @param <PROPERTY_CLASS>
	 */
	public abstract class PropertyDestination<PROPERTY_CLASS>{
		Class<PROPERTY_CLASS>propertyClass; public Class<PROPERTY_CLASS>getPropertyClass(){return propertyClass;}
		public PropertyDestination(Class<PROPERTY_CLASS> propertyClass){this.propertyClass=propertyClass;}
		public abstract void set(PROPERTY_CLASS value, ThirdPartyParseable bean);
		public abstract PROPERTY_CLASS get(ThirdPartyParseable bean);
		
		/**
		 * Returns the property's value as a non-java-style string. <br>If property is boolean, true will be "1", false will be "0".<br>
		 * Enums are represented as strings of their ordinal numbers, not their names.
		 * @param bean
		 * @return
		 * @since Sep 17, 2012
		 */
		public String getAsString(ThirdPartyParseable bean){
			Object result = get(bean);
			if(result.getClass().isEnum())return (((Enum)result).ordinal()+"");
			else if(result.getClass()==boolean.class || result.getClass()==Boolean.class)return ((Boolean)result)?"1":"0";
			return result.toString();
			}
		}//end PropertyDestination
		
	/**
	 * Returns a PropertyDestination representing the given property name and type.
	 * @param propertyName
	 * @param elementType
	 * @return
	 * @since Sep 17, 2012
	 */
	public<PROPERTY_CLASS> PropertyDestination<PROPERTY_CLASS> 
			property(final String propertyName, final Class <PROPERTY_CLASS>elementType){
		return new PropertyDestination<PROPERTY_CLASS>(elementType){
			@Override
			public void set(PROPERTY_CLASS value, ThirdPartyParseable bean){
				Parser.this.set(bean, propertyName, value, elementType);
				}

			@Override
			public PROPERTY_CLASS get(ThirdPartyParseable bean){
				return Parser.this.get(bean, propertyName, elementType);
				}
			};
		}//end property(...)
			
	/**
	 * Returns a PropertyDestination representing the given property name, type, and index given the bean property is indexed.<br>
	 * When reading, will automatically re-size (re-allocate, really) the array if the index too high, then add the items, Expect this to be slow.
	 * @param propertyName	The name of the property, not including the 'get' or 'set' prefix.
	 * @param elementType	The type of element to be set at the given index.
	 * @param index	Index in the array or List at which to perform the set or get.
	 * @return	An indexed PropertyDestination with the supplied traits.
	 * @since Sep 17, 2012
	 */
	public<PROPERTY_CLASS extends Object> PropertyDestination<PROPERTY_CLASS> 
			indexedProperty(final String propertyName,final Class<PROPERTY_CLASS>elementType,final int index){
		return new PropertyDestination<PROPERTY_CLASS>(elementType){
						@Override
						public void set(PROPERTY_CLASS value,
								ThirdPartyParseable bean){
							//System.out.println("elementType="+elementType);
							Object nilArray = Array.newInstance(elementType, 0);//TODO: Move this allocation to later branch
							Object array = Parser.this.get(bean, propertyName, nilArray.getClass());
							Class indexingClass=null;
							try{indexingClass=Parser.this.getPropertyReturnType(bean, propertyName);}
							catch(NoSuchMethodException e){e.printStackTrace();System.exit(1);}
							if(List.class.isAssignableFrom(indexingClass))
								{//Lists scale up far better than arrays.
								if(array==null)
									{//Not yet initialized
									array=new ArrayList();
									Parser.this.set(bean,propertyName, array, indexingClass);//Install new
									}
								final List<PROPERTY_CLASS> list = (List<PROPERTY_CLASS>)array;
								list.add(index,value);
								}
							else{
								if(array==null)array= Array.newInstance(elementType, 1);
								assert array!=null:"array should not be null at this point. Trouble ahead.";
								//Guaranteed an array here
								if(index<0)throw new IndexOutOfBoundsException(""+index);
								if(index<Array.getLength(array)-1)
									{//Set the value
									Array.set(array, index, value);
									//array[arrayIndex]=value;
									}
								else{//Need to resize
									nilArray = Array.newInstance(elementType, index+1);
									System.arraycopy(array, 0, nilArray, 0, Array.getLength(array));
									array=nilArray;
									Array.set(array, index, value);
									}
								if(array==null) throw new NullPointerException("array should not be null at this point. Trouble ahead.");
								//Update
								Parser.this.set(bean, propertyName, array, array.getClass());
								}
							}//end set(...)

						@Override
						public PROPERTY_CLASS get(ThirdPartyParseable bean)
							{
							Class indexingClass=null;
							try{indexingClass=Parser.this.getPropertyReturnType(bean, propertyName);}
							catch(NoSuchMethodException e){e.printStackTrace();System.exit(1);}
							if(List.class.isAssignableFrom(indexingClass))
								{List<PROPERTY_CLASS> list = (List<PROPERTY_CLASS>)Parser.this.get(bean, propertyName,indexingClass);
								return list.get(index);
								}
							else{final Class<PROPERTY_CLASS> arrayClass = (Class<PROPERTY_CLASS>)(Array.newInstance(elementType, 0).getClass());
								PROPERTY_CLASS result = (PROPERTY_CLASS)Array.get((Parser.this.get(bean, propertyName,arrayClass)),index);
								//System.out.println("indexedProperty.get("+arrayIndex+") returning "+result);
								return result;
								}
							}//end get(...)
					};
		}//end property(...)

			/**
			 * Tells the Parser that there is an arbitrary-length string in the current position in the description until it finds the specified ending. When writing with includeEndingWhenReading false, the ending is automatically
			 * written to the file.
			 * @param ending
			 * @param property						The bean property to map to/from this String.
			 * @param includeEndingWhenReading		Include the specified ending when reading, and do not write the specified ending when writing, assuming that it is already in the property's String.
			 * @since Sep 17, 2012
			 */
			public <CLASS>void stringEndingWith(final String ending,final PropertyDestination<CLASS> property,
					final boolean includeEndingWhenReading){
			    stringEndingWith(ending!=null?new String[]{ending}:null,property,includeEndingWhenReading);
			}

	/**
	 * Tells the Parser that there is an arbitrary-length string in the current position in the description until it finds the specified ending. When writing with includeEndingWhenReading false, the ending is automatically
	 * written to the file.
	 * @param ending
	 * @param property						The bean property to map to/from this String.
	 * @param includeEndingWhenReading		Include the specified ending when reading, and do not write the specified ending when writing, assuming that it is already in the property's String.
	 * @since Sep 17, 2012
	 */
	public <CLASS>void stringEndingWith(String[] endings,final PropertyDestination<CLASS> property,
			final boolean includeEndingWhenReading){
	    if(endings!=null){
		if(endings.length>0)
	         Arrays.sort(endings, new Comparator<String>(){
		    @Override
		    public int compare(String l, String r) {//Reversed; sort large to small
			return r.length()-l.length();
		    }});
		else endings=null;
		}//end if(null)
	    final String [] ending = endings;
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					String string ="";
					if(ending!=null){
					    	String endingFound=null;
					    	while(endingFound==null){
					    	 string+=((char)is.readByte());
					    	 for(String test:ending)
					    	     if(string.endsWith(test)){
					    		 endingFound=test; break;
					    		 }//end for(endings)
					    	}//end while(!endingFound)
						if(!includeEndingWhenReading)
							{string=string.substring(0, (string.length()-endingFound.length()));}
						}//end if(ending!=null)
					else{// null.
						try {while(true)//heheh.
								{string+=((char)is.readByte());}
							}//end try{}
						catch(EOFException e){}//This is to detect the EOF.
						if(!includeEndingWhenReading)
							{string=string.substring(0, (string.length()));}
						}//end null-ending (go until EOF)
					//System.out.println("property.getPropertyClass()="+property.getPropertyClass());
					//System.out.println("stringEndingWith got "+string);
					property.set(convertFromString(string,property.getPropertyClass()), bean);
					//set(bean,targetProperty,string,propertyType);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.write(property.getAsString(bean).getBytes());
					if(ending!=null)
					    if(ending[0]!=null)
						os.write(ending[0].getBytes());
					}
			}.go();
		}//end stringEndingWith(...)
	
	/**
	 * Tells the parser that there is a String at the current position, ending with the specified pattern.
	 * @param ending	String pattern describing the ending of this string.
	 * @param sParser	Special String parser to convert said String into some other type of Object
	 * @param property	Target bean PropertyDestination to which to apply this relation.
	 * @param includeEndingWhenReading
	 * @since Dec 10, 2013
	 */
	
	public <CLASS>void stringEndingWith(final String ending,final StringParser sParser,final PropertyDestination<CLASS> property,
			final boolean includeEndingWhenReading){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					String string ="";
					if(ending!=null)
						{
						while(!string.endsWith(ending))
							{string+=((char)is.readByte());}
						if(!includeEndingWhenReading)
							{string=string.substring(0, (string.length()-ending.length()));}
						}//end if(ending!=null)
					else{// null.
						try {while(true)//heheh.
								{string+=((char)is.readByte());}
							}//end try{}
						catch(EOFException e){}//This is to detect the EOF.
						if(!includeEndingWhenReading){
							string=string.substring(0, (string.length()));
							}
						}//end null-ending (go until EOF)
					//System.out.println("property.getPropertyClass()="+property.getPropertyClass());
					property.set((CLASS)sParser.parseRead(string), bean);
					//set(bean,targetProperty,string,propertyType);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					os.write(sParser.parseWrite(bean).getBytes());
					//os.write(property.getAsString(bean).getBytes());
					if(ending!=null)os.write(ending.getBytes());
					}
			
			}.go();
		}//end stringEndingWith(...)
	
	/**
	 * Tells the Parser that a non-primitive object is to be found at the current location in the description, 
	 * mapped to the supplied property. The Parser then attempts to map/parse this object as a ThirdPartyParseable.
	 * @param pDest				The property to be mapped, typically an array property whose element class is same as those in 'inclusions'
	 * @param inclusions		The class or classes to attempt to use when reading (automatically determined when writing)
	 * @since Sep 17, 2012
	 */
	public <CLASS extends ThirdPartyParseable>void subParseProposedClasses(final PropertyDestination<CLASS> pDest, final ClassInclusion ... inclusions){
		new RWHelper(){
			@Override
			public void read(EndianAwareDataInputStream is, 
					ThirdPartyParseable bean) throws IOException{
				ArrayList<Class> classes = new ArrayList<Class>();
				//System.out.println("read()...");
				for(ClassInclusion inc:inclusions)
					{Collections.addAll(classes, inc.propose());}
				if(classes.size()==0)throw new RuntimeException("No inclusion classes given. Need at least one. Trouble ahead...");
				CLASS obj=null;
				for(Class c:classes){
					//System.out.println("Parser.subParseProposedClasses() trying class "+c.getName());
					try{obj=(CLASS)readToNewBean(is, (Class<? extends ThirdPartyParseable>)c);break;}//break from the loop if successful.
					catch(IllegalAccessException e){e.printStackTrace();}
					catch(UnrecognizedFormatException e){}//keep trying other parser classes if not successful
					}
				//Store the object
				//System.out.println("subParseProposedClasses() obj="+obj+" bean="+bean);
				if(obj!=null){pDest.set(obj, bean);}
				//Fail
				else{
					long readTally=is.getReadTally();
					System.out.println("None of the supplied classes match at byte "+readTally+" (0x"+Long.toHexString(readTally).toUpperCase()+")");new Exception().printStackTrace();
					}
				}//end read(...)

			@Override
			public void write(EndianAwareDataOutputStream os, 
					ThirdPartyParseable bean) throws IOException{
				if(bean==null)throw new NullPointerException();
				if(pDest==null)throw new NullPointerException();
				if(pDest.get(bean)==null)throw new NullPointerException();
				writeBean(pDest.get(bean),os);
				}
			}.go();
		}//end subParseProposedClasses(...)
	/*
	public <CLASS>void stringsEndingWithToArray
		(final String ending, final String delimiter, final int count, final String targetPropertyArray,
				final Class<CLASS> propertyElementType, final boolean includeEndingWhenReading)
		{
		new RWHelper()
			{
				@Override
				public void read(DataInputStream is, 
						ThirdPartyParseable bean) throws IOException
					{
					ArrayList<CLASS>resultList = new ArrayList<CLASS>();
					for(int i=0; i<count; i++)
						{
						String string ="";
						if(ending!=null)
							{
							if(i<count-1)
								{
								while(!string.endsWith(delimiter))
									{
									string+=((char)is.readByte());
									//System.out.println(string);
									}
								if(!includeEndingWhenReading)
									{
									string=string.substring(0, (string.length()-delimiter.length()));
									}
								}//end if(delimiter)
							else
								{//ending
								while(!string.endsWith(ending))
									{
									string+=((char)is.readByte());
									//System.out.println(string);
									}
								if(!includeEndingWhenReading)
									{
									string=string.substring(0, (string.length()-ending.length()));
									}
								}
							}//end if(ending!=null)
						else 
							{// null.
							try
								{
								while(true)//heheh.
									{
									string+=((char)is.readByte());
									//System.out.println(string);
									}
								}//end try{}
							catch(EOFException e){}//This is to detect the EOF.
							if(!includeEndingWhenReading)
								{
								string=string.substring(0, (string.length()));
								}
							}//end null-ending (go until EOF)
						resultList.add(convertFromString(string,propertyElementType));
						//set(bean,targetProperty,string,propertyType);
						}//end for(count)
					//Set the array property
					
					set(bean, targetPropertyArray, 
							(CLASS [])Array.newInstance(propertyElementType, resultList.size()), propertyElementType);
					}//end read()

				@Override
				public void write(DataOutputStream os, 
						ThirdPartyParseable bean) throws IOException
					{
					CLASS [] elements = (CLASS [])get(bean,targetPropertyArray,propertyElementType);
					for(int i=0; i<elements.length;i++)
						{
						os.write(elements[i].toString().getBytes());
						if(ending!=null)
							{
							if(i<elements.length-1) 	os.write(ending.getBytes());
							else						os.write(delimiter.getBytes());
							}//end if(ending!=null)
						}//end for(count)
					//os.write(get(bean,targetPropertyArray,String.class).getBytes());
					}//end write(...)
			
			}.go();
		}//end stringsEndingWithToArray
	*/
	/**
	 * Maps a series of comma-separated ASCII strings with the specified ending to multiple properties in the same order.
	 * @param ending
	 * @param propertyType
	 * @param includeEndingWhenReading
	 * @param properties
	 * @since Sep 17, 2012
	 */
	public void stringCSVEndingWith(final String ending, final Class<?> propertyType, final boolean includeEndingWhenReading,String ... properties){
		for(int i=0; i<properties.length-1;i++)
			{stringEndingWith(",",property(properties[i],propertyType),includeEndingWhenReading);}
		stringEndingWith(ending,property(properties[properties.length-1],propertyType),includeEndingWhenReading);
		}//end stringCSVEndingWith(...)
		
	/*
	public void stringEndingWith(final String ending, final String targetProperty, final Class<?> propertyType, final boolean includeEndingWhenReading)
		{
		new RWHelper()
			{
				@Override
				public void read(DataInputStream is, 
						ThirdPartyParseable bean) throws IOException
					{
					String string ="";
					if(ending!=null)
						{
						while(!string.endsWith(ending))
							{
							string+=((char)is.readByte());
							//System.out.println(string);
							}
						if(!includeEndingWhenReading)
							{
							string=string.substring(0, (string.length()-ending.length()));
							}
						}//end if(ending!=null)
					else 
						{// null.
						try
							{
							while(true)//heheh.
								{
								string+=((char)is.readByte());
								//System.out.println(string);
								}
							}//end try{}
						catch(EOFException e){}//This is to detect the EOF.
						if(!includeEndingWhenReading)
							{
							string=string.substring(0, (string.length()));
							}
						}//end null-ending (go until EOF)
					set(bean,targetProperty,string,propertyType);
					}

				@Override
				public void write(DataOutputStream os, 
						ThirdPartyParseable bean) throws IOException
					{
					os.write(get(bean,targetProperty,String.class).getBytes());
					if(ending!=null)os.write(ending.getBytes());
					}
			
			}.go();
		}//end stringendingWith(...)
	*/
	
	/**
	 * Maps an ASCII string of specified length in the current position in the description, to the given
	 * bean property.
	 * @param propertyNumBytes
	 * @param dest
	 * @since Sep 17, 2012
	 */
	public<CLASS> void stringOfLength(final int propertyNumBytes, final PropertyDestination<CLASS> dest){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					byte [] buffer = new byte[propertyNumBytes];
					is.readFully(buffer);
					//Strip non-print chars
					boolean ended=false;
					for(int i=0; i<buffer.length; i++)
						{if(buffer[i]==0) ended=true;if(ended)buffer[i]=0;}
					String data = new String(buffer).replaceAll("\\p{C}", "");
					
					dest.set(convertFromString(data,dest.getPropertyClass()), bean);
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					//os.write(get(bean,targetProperty,String.class).getBytes());
					byte [] raw = dest.get(bean).toString().getBytes();
					byte [] result = new byte[propertyNumBytes];
					//TODO: Use arraycopy
					for(int i=0; i<result.length;i++){
						try{result[i]=raw[i];}
						catch(ArrayIndexOutOfBoundsException e)
							{result[i]=0x0;}
						}//end for(result.length)
					os.write(result);
					}//end write(...)
			
			}.go();
		}//end stringOfLength(...)
	
	/*
	public ParseOperation For(
			final String countProperty,final ParseOperation [] operations, String indexPseudoVariableName)
		{
		return new ParseOperation()
			{
				@Override
				public void read(DataInputStream is, ThirdPartyParseable target)
						throws IntrospectionException, IOException
					{
					final int count = getInt(target,countProperty);
					for(int i=0; i<count; i++)
						{
						setPseudoVariable(indexPseudoVariableName, i);
						for(ParseOperation operation:operations)
							{
							operation.read(is, target);
							//ThirdPartyParseable obj = readBean(is,clazz);
							//indexedSetObject(target,indexedSetterPropertyName,i,obj);
							}
						}//end for(count)
					}//end read(...)

				@Override
				public void write(DataOutputStream os,
						ThirdPartyParseable source)
						throws IntrospectionException, IOException
					{
					final int count = getInt(source,countProperty);
					for(int i=0; i<count; i++)
						{
						for(ParseOperation operation: operations)
							{
							operation.write(os, source);
							//writeBean((ThirdPartyParseable)indexedGetObject(source,indexedSetterPropertyName,i),os);
							}
						}//end for(count)
					}//end write(...)
			};
		}//end For(...)
	*/
	
	/**
	 * Tells the parser that the current position in the description contains a series of parseable objects which may or may not be primitive.
	 * @param count					Number of objects to parse.
	 * @param arrayOrListPropertyName		Array-type or List property to which this data is mapped.
	 * @param elementClass			The element type for the array-type property being mapped.
	 * @since Sep 17, 2012
	 */
	public <CLASS extends ThirdPartyParseable> void arrayOf(final int count, final String arrayOrListPropertyName, final Class <CLASS> elementClass){
		new RWHelper(){
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException{
					//final int count = get(bean,countProperty,Integer.class);
					ArrayList<CLASS>objectsToMake = new ArrayList<CLASS>();

					Class indexingClass=null;
					try{indexingClass=Parser.this.getPropertyReturnType(bean, arrayOrListPropertyName);}
					catch(NoSuchMethodException e){e.printStackTrace();System.exit(1);}
					
					for(int i=0; i<count; i++){
						try{objectsToMake.add(readToNewBean(is, elementClass));}
						catch(IllegalAccessException e){e.printStackTrace();}
						catch(UnrecognizedFormatException e){e.printStackTrace();}
						}//end for(count)
					if(List.class.isAssignableFrom(indexingClass)) set(bean,arrayOrListPropertyName,objectsToMake,null);
					else set(bean,arrayOrListPropertyName,objectsToMake.toArray((CLASS [])Array.newInstance(elementClass, 0)),null);
					}//end read(...)

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException{
					
					Class indexingClass=null;
					try{indexingClass=Parser.this.getPropertyReturnType(bean, arrayOrListPropertyName);}
					catch(NoSuchMethodException e){e.printStackTrace();System.exit(1);}
					
					if(List.class.isAssignableFrom(indexingClass)) 
						{List<CLASS> list = (List<CLASS>)get(bean,arrayOrListPropertyName, indexingClass);
						for(CLASS item:list)
							{writeBean(item,os);}
						}
					else{
						CLASS [] array = get(bean,arrayOrListPropertyName, (Class<CLASS []>)Array.newInstance(elementClass, 0).getClass());
						for(CLASS item:array)
							{writeBean(item,os);}
						}//end if(array)
					}//end write(...)
			}.go();
		}//end arrayOf(...)
/*
	public <CLASS extends ThirdPartyParseable> void subParse(final String propertyName, final Class<CLASS> clazz)
		{
		new RWHelper()
			{
				@Override
				public void read(EndianAwareDataInputStream is, 
						ThirdPartyParseable bean) throws IOException
					{
					try{set(bean,propertyName,readToNewBean(is, clazz),clazz);}
					catch(IllegalAccessException e){e.printStackTrace();}
					catch(UnrecognizedFormatException e){e.printStackTrace();}
					}

				@Override
				public void write(EndianAwareDataOutputStream os, 
						ThirdPartyParseable bean) throws IOException
					{
					writeBean(get(bean,propertyName, clazz),os);
					}//end write(...)
			
			}.go();
		}//end subParse(...)
	*/

	public void dumpState(){
		System.err.println("Dumping state:");
		new Exception().printStackTrace();
		System.err.println("current read tally: "+is.getReadTally()+" bytes. Or offset 0x"+Long.toHexString(is.getReadTally()));
		System.err.println("Bean stack: ");
		for(ThirdPartyParseable b: beanStack){
			System.err.println("\t"+b.getClass().getSimpleName());
			}
		}//end kaboom
	}//end Parser
