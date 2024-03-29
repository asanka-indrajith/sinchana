/************************************************************************************

 * Sinchana Distributed Hash table 

 * Copyright (C) 2012 Sinchana DHT - Department of Computer Science &               
 * Engineering, University of Moratuwa, Sri Lanka. Permission is hereby 
 * granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files of Sinchana DHT, to deal 
 * in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * Neither the name of University of Moratuwa, Department of Computer Science 
 * & Engineering nor the names of its contributors may be used to endorse or 
 * promote products derived from this software without specific prior written 
 * permission.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.                                                                    
 ************************************************************************************/
package sinchana.service;

import sinchana.SinchanaCallBack;

/**
 *This interface is to be implemented by any class that provide services.
 * @author Hirantha Subasinghe
 */
public interface SinchanaServiceInterface extends SinchanaCallBack {

	/**
	 * This method will be called when the service is invoked. The 
	 * service key and the data to be processed are passed as arguments. Output 
	 * from data processing is returned as a byte array
	 * @param serviceKey byte array service key
	 * @param data byte array data. This can be <code>null</code>.
	 * @return byte array response to be sent back to the requester. Return 
	 * <code>null</code> if nothing to be sent back.
	 */
	public abstract byte[] process(byte[] serviceKey, byte[] data);

	/**
	 * This method will be called when service is published. The service key and 
	 * the state will be passed as arguments.
	 * @param serviceKey byte array service key.
	 * @param success <code>true</code> if the service is published successfully, 
	 * <code>false</code> otherwise.
	 */
	public abstract void isPublished(byte[] serviceKey, Boolean success);

	/**
	 * This method will be called when service is removed. The service key and 
	 * the state will be passed as arguments.
	 * @param serviceKey byte array service key.
	 * @param success <code>true</code> if the service is removed successfully, 
	 * <code>false</code> otherwise.
	 */
	public abstract void isRemoved(byte[] serviceKey, Boolean success);
    
}
