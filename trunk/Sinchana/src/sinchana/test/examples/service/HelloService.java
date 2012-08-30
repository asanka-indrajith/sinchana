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
package sinchana.test.examples.service;

/**
 *This class is an example service implementation.
 * @author Hirantha Subasinghe
 */
public class HelloService implements sinchana.service.SinchanaServiceInterface {

	@Override
	public byte[] process(byte[] serviceKey, byte[] data) {
		String res = (data != null ? "Hi " + new String(data) + ", " : "")
				+ "Greetings from " + new String(serviceKey);
		System.out.println(new String(serviceKey)
				+ ":\treq: " + (data != null ? new String(data) : "null")
				+ "\tres: " + res);
		return res.getBytes();
	}

	@Override
	public void isPublished(byte[] serviceKey, Boolean success) {
		System.out.println(new String(serviceKey) + " is published");
	}

	@Override
	public void isRemoved(byte[] serviceKey, Boolean success) {
		System.out.println(new String(serviceKey) + " is removed");
	}

	@Override
	public void error(byte[] error) {
		System.out.println("Error with Hello Service: " + new String(error));
	}
}
