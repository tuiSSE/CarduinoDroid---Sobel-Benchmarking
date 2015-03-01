#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <stdlib.h>

//#include "../libs/av/include/camera/Camera.h"

//Java_com_example_sobel_OverlayView_doNativeProcessing

		int test(){
			int blubb = 15*9;
			return blubb;
		}

extern "C" {
	JNIEXPORT void JNICALL
	Java_swp_tuilmenau_carduinodroid_Filter_Sobel_doNativeSobel(JNIEnv *env, jobject  obj,
			jbyteArray frame, jint width, jint height, jobject diff) {

			jboolean framecopy;

			jint *dbuf = (jint*) env->GetDirectBufferAddress(diff);
			jbyte *fbuf = env->GetByteArrayElements(frame,&framecopy);

			int x, y, maxx=width-1, maxy=height-1, p=width+1, px, py, ps;

			/*  2d convolution matrices

			        -1 0 1
			   px = -2 0 2   * A
			        -1 0 1

			        1  2  1
			   py = 0  0  0  * A
			       -1 -2 -1


			   Point(x,y) == PointArray[x + y * width]
			*/

			//adjustable threshold
			//handles strictness
			const int threshold = 90;

			for(y=1; y<maxy; y++, p+=2){
				for(x=1; x<maxx; x++, p++){
					px = fbuf[p+width+1]-fbuf[p+width-1]+fbuf[p+1]+fbuf[p+1]-fbuf[p-1]-fbuf[p-1]+fbuf[p-width+1]-fbuf[p-width-1];
					py = fbuf[p-width-1]+fbuf[p-width]+fbuf[p-width]+fbuf[p-width+1]-fbuf[p+width-1]-fbuf[p+width]-fbuf[p+width]-fbuf[p+width+1];

					//avoid negative values
					ps=abs(px)+abs(py);

					//binarize
					if(ps>threshold) {
						ps=255;
					} else {
						ps=0;
					}


					//create correct value for every channel
					dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
				}
			}
			//do some cleanup to prevent heap from growing
			env->ReleaseByteArrayElements(frame, fbuf, JNI_ABORT);
			//free(dbuf);
		}

	JNIEXPORT int JNICALL
		Java_swp_tuilmenau_carduinodroid_Filter_Sobel_doNativeSobelYUV(JNIEnv *env, jobject  obj,
				jbyteArray frame, jint width, jint height, jobject diff) {

				jboolean framecopy;

				jbyte *dbuf = (jbyte*) env->GetDirectBufferAddress(diff);
				jbyte *fbuf = env->GetByteArrayElements(frame,&framecopy);

				int x, y, maxx=width-1, maxy=height-1, p=width+1, px, py, ps;

				/*  2d convolution matrices

				        -1 0 1
				   px = -2 0 2   * A
				        -1 0 1

				        1  2  1
				   py = 0  0  0  * A
				       -1 -2 -1


				   Point(x,y) == PointArray[x + y * width]
				*/

				//adjustable threshold
				//handles strictness
				const int threshold = 90;

				int counter = 0;

				for(y=1; y<maxy; y++, p+=2){
					for(x=1; x<maxx; x++, p++){
						px = fbuf[p+width+1]-fbuf[p+width-1]+fbuf[p+1]+fbuf[p+1]-fbuf[p-1]-fbuf[p-1]+fbuf[p-width+1]-fbuf[p-width-1];
						py = fbuf[p-width-1]+fbuf[p-width]+fbuf[p-width]+fbuf[p-width+1]-fbuf[p+width-1]-fbuf[p+width]-fbuf[p+width]-fbuf[p+width+1];

						//avoid negative values
						ps=abs(px)+abs(py);

						//binarize
						if(ps>threshold) {
							//dbuf[p] = 0xFFFF;
							ps = 255;
							counter++;
						} else {
							//dbuf[p] = 0x0;
							ps = 0;
						}

						//int ps_g = 63;

						//create correct value for every channel
						//dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
						//dbuf[p] = (ps<11)|(ps_g<<5)|ps; //565
						dbuf[p] = ps;
					}
				}
				int start = width * height;  //U and V start after (width * height) Y bytes
				int size = (int) (start * 1.5);  //there's one U and one V byte for every 4 pixel block in the frame
				for(int i = start; i<size; ++i){
					dbuf[i] = 127;  //set U and V to 127
				}

				env->ReleaseByteArrayElements(frame, fbuf, JNI_ABORT);

				return counter;
			}

//	JNIEXPORT void JNICALL
//			Java_swp_tuilmenau_carduinodroid_Filter_Sobel_doNativeSobel2(JNIEnv *env, jobject  obj,
//					jbyteArray frame, jint width, jint height, jobject diff) {
//
//					jboolean framecopy;
//
//					jbyte *dbuf = (jshort*) env->GetDirectBufferAddress(diff);
//					jbyte *fbuf = env->GetByteArrayElements(frame,&framecopy);
//
//					int x, y, maxx=width-1, maxy=height-1, p=width+1, px, py, ps;
//
//					/*  2d convolution matrices
//
//					        -1 0 1
//					   px = -2 0 2   * A
//					        -1 0 1
//
//					        1  2  1
//					   py = 0  0  0  * A
//					       -1 -2 -1
//
//
//					   Point(x,y) == PointArray[x + y * width]
//					*/
//
//					//adjustable threshold
//					//handles strictness
//					const int threshold = 90;
//
//					for(y=1; y<maxy; y++, p+=2){
//						for(x=1; x<maxx; x++, p++){
//							px = fbuf[p+width+1]-fbuf[p+width-1]+fbuf[p+1]+fbuf[p+1]-fbuf[p-1]-fbuf[p-1]+fbuf[p-width+1]-fbuf[p-width-1];
//							py = fbuf[p-width-1]+fbuf[p-width]+fbuf[p-width]+fbuf[p-width+1]-fbuf[p+width-1]-fbuf[p+width]-fbuf[p+width]-fbuf[p+width+1];
//
//							//avoid negative values
//							ps=abs(px)+abs(py);
//
//							//binarize
//							if(ps>threshold) {
//								dbuf[p] = 0xFFFF;
//							} else {
//								dbuf[p] = 0x0;
//							}
//
//							//int ps_g = 63;
//
//							//create correct value for every channel
//							//dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
//							//dbuf[p] = (ps<11)|(ps_g<<5)|ps; //565
//
//						}
//					}
//					//do some cleanup to prevent heap from growing
//					//env->ReleaseByteArrayElements(frame, fbuf, JNI_ABORT);
//					//free(dbuf);
//				}

		}
