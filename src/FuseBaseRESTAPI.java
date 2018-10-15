/*
 * Copyright (c) 2018 "Niclas Kjäll-Ohlsson, Bjørnar Fjøren"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.StringBuffer;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/*
** FuseBase ReST API:
** 	Call api() method to list API
** 	http://hostname:port/api will list api endpoints and parameters as JSON
**
**	Example call to schedule a script to run each 10 seconds starting immediately:
**		http://localhost:4444/scheduler/jobs/create?name=testjob&scriptName=test&initialDelay=0&period=10&timeUnit=SECONDS
*/

public class FuseBaseRESTAPI {
	
	private FuseBase FuseBase;
	private URLDecoder ud;
	private HashMap<String, FuseBaseRESTAPIMethod> httpEndpointMethods;
	private JSONBuilderDynamic apiMethodDescriptor;
	
	private JSONBuilder apiEndpointsJSONBuilder;
	
	/* BEGIN Annotation definitions for ReST API */
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface HTTP_ENDPOINT {
		boolean requiresAuthentication() default true;
	}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface API {
		HttpRequestType type() default HttpRequestType.GET;
		boolean requiresAuthentication() default true;
	}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface GET {}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface POST {}
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Parameter {
		String name();
		boolean required() default true;
		Class validValuesClass() default Object.class;
	}
	
	private static final String API_PREFIX = "api/";
	
	/* END Annotation definitions for ReST API */
	
	public FuseBaseRESTAPI(FuseBase FuseBase) throws Exception {
		
		this.FuseBase = FuseBase;
		
		this.ud =
			new URLDecoder();
			
		this.httpEndpointMethods =
			new HashMap<String, FuseBaseRESTAPIMethod>();
			
		this.init();
		
	}
	
	private HashMap<String, Annotation> getAnnotationMap(Annotation[] annotations) {
		
		HashMap<String, Annotation> annotationMap =
			new HashMap<String, Annotation>();
			
		for(Annotation annotation : annotations) {
			
			annotationMap.put(
				annotation.annotationType().getSimpleName(),
				annotation
			);
			
		}
		
		return annotationMap;
		
	}
	
	private void init() throws Exception {
		
		Annotation methodAnnotation, parameterAnnotation;
		
		HashMap<String, Annotation>
			annotationMap,
			parameterAnnotationMap;
		
		Class[] parameterTypes;
		
		Method[] methods =
			this.getClass().getMethods();
			
		Arrays.sort(
			methods,
			new Comparator<Method>() {
				public int compare(Method a, Method b) {
					return a.getName().compareTo(b.getName());
				}
			}
		);
		
		this.apiMethodDescriptor = JSONBuilderDynamic.f();
		
		apiMethodDescriptor.$('[');
		
		for(Method method : methods) {
			
			annotationMap =
				this.getAnnotationMap(
					method.getAnnotations()
				);
			
			String apiPath =
				method.getName().replaceAll("_", "/");
			
			if(!apiPath.equals("api")) {
				apiPath = FuseBaseRESTAPI.API_PREFIX + apiPath;
			}
			
			if(annotationMap.containsKey("HTTP_ENDPOINT")) {
				
				methodAnnotation =
					annotationMap.get("HTTP_ENDPOINT");
				
				boolean requiresAuthentication =
					true;
				
				// List API annotation attributes
				for(Method annotationMethod : methodAnnotation.annotationType().getDeclaredMethods()) {
					
					if(annotationMethod.getName().equals("requiresAuthentication")) {
						
						Object annotationMethodValue =
							annotationMethod.invoke(
								methodAnnotation,
								new Object[]{}
							);
							
						requiresAuthentication =
							((Boolean)annotationMethodValue).booleanValue();
						
					}
				
				}
				
				// Register endpoint for routing
				this.httpEndpointMethods.put(
					apiPath,
					new FuseBaseRESTAPIMethod(
						apiPath,
						method,
						requiresAuthentication
					)
				);
				
			} else if(annotationMap.containsKey("API")) {
				
				methodAnnotation =
					annotationMap.get("API");
				
				parameterTypes =
					method.getParameterTypes();
				
				apiMethodDescriptor.$('{');
				
				apiMethodDescriptor.k("endpoint").v(apiPath);
				
				// List API annotation attributes
				for(Method annotationMethod : methodAnnotation.annotationType().getDeclaredMethods()) {
					
					Object annotationMethodValue =
						annotationMethod.invoke(
							methodAnnotation,
							new Object[]{}
						);
				
					apiMethodDescriptor.k(annotationMethod.getName()).v(annotationMethodValue);
				
				}
				
				apiMethodDescriptor.k("parameters");
				apiMethodDescriptor.$('[');
				
				for(int i=0; i<parameterTypes.length; i++) {
					
					if(parameterTypes[i].getSimpleName().equals(FuseBaseRESTAPICall.class.getSimpleName())) {
						continue;
					}
					
					parameterAnnotationMap =
						this.getAnnotationMap(
							method.getParameterAnnotations()[i]
						);
						
					apiMethodDescriptor.$('{');
					
					// Type of parameter
					apiMethodDescriptor.k("type").v(parameterTypes[i].getSimpleName());
					
					// If the parameter is an enum then list enum constants as valid values
					if(parameterTypes[i].getEnumConstants() != null) {
						
						apiMethodDescriptor.k("valid_values");
						
						apiMethodDescriptor.$('[');
						
						for(Object validValue : parameterTypes[i].getEnumConstants()) {
							apiMethodDescriptor.v(validValue);
						}
						
						apiMethodDescriptor.$(']');
						
					}
					
					// List Parameter annotation attributes
					if(parameterAnnotationMap.containsKey("Parameter")) {
						
						parameterAnnotation =
							parameterAnnotationMap.get("Parameter");
						
						for(Method parameterAnnotationMethod : parameterAnnotation.annotationType().getDeclaredMethods()) {

							Object parameterAnnotationMethodValue =
								parameterAnnotationMethod.invoke(
									parameterAnnotation,
									new Object[]{}
								);
							
							if(	parameterAnnotationMethod.getName().equals("validValuesClass") &&
								((Class)parameterAnnotationMethodValue).getSimpleName().equals("Object")	) {
								
								continue;
								
							} else if(	parameterAnnotationMethod.getName().equals("validValuesClass") &&
										!((Class)parameterAnnotationMethodValue).getSimpleName().equals("Object")	) {
								
								String validValuesClass =
									((Class)parameterAnnotationMethodValue).getSimpleName();
								
								if(validValuesClass.equals("DBConnectionManager")) {
									
									apiMethodDescriptor.k(
										"valid_values"
									).v(
										this.FuseBase.dbConnectionManager
									);
									
								} else if(validValuesClass.equals("ScriptManager")) {
									
									apiMethodDescriptor.k(
										"valid_values"
									).v(
										this.FuseBase.scriptManager
									);
									
								} else if(validValuesClass.equals("QueryManager")) {
									
									apiMethodDescriptor.k(
										"valid_values"
									).v(
										this.FuseBase.queryManager
									);
									
								} else if(validValuesClass.equals("Scheduler")) {
									
									apiMethodDescriptor.k(
										"valid_values"
									).v(
										this.FuseBase.scheduler()
									);
									
								}
								
								
							} else {
								
								apiMethodDescriptor.k(
									parameterAnnotationMethod.getName()
								).v(
									parameterAnnotationMethodValue
								);
								
							}
							
							

						}
						
					}
					
					apiMethodDescriptor.$('}');
					
				}
				
				apiMethodDescriptor.$(']');
				
				apiMethodDescriptor.$('}');
				
			}
			
		}
		
		apiMethodDescriptor.$(']');
		
		this.listPossibleAPIPrivileges();
		
	}
	
	private void listPossibleAPIPrivileges() {
		
		// Set API endpoints JSONBuilder to be used for listing possible api privileges
		
		FuseBaseRESTAPIMethod[] FuseBaseRESTAPIMethods =
			this.httpEndpointMethods.values().toArray(new FuseBaseRESTAPIMethod[0]);
			
		Arrays.sort(FuseBaseRESTAPIMethods);
		
		this.apiEndpointsJSONBuilder = JSONBuilder.f();
		
		this.apiEndpointsJSONBuilder.$('[');
		
		for(FuseBaseRESTAPIMethod FuseBaseRESTAPIMethod : FuseBaseRESTAPIMethods) {
			
			if(!FuseBaseRESTAPIMethod.requiresAuthentication()) {
				
				continue;
				
			}
			
			this.apiEndpointsJSONBuilder.v(
				FuseBaseRESTAPIMethod.apiPath()
			);
			
		}
		
		this.apiEndpointsJSONBuilder.$(']');
		
	}
	
	public boolean hasRoute(String aPIPath) {
		
		return this.httpEndpointMethods.containsKey(aPIPath);
		
	}
	
	/*
	** API mapper
	*/
	public boolean route(	String aPIPath,
							DataOutputStream output,
							HttpRequest httpRequest,
							HTTPServerThread httpServerThread	) throws Exception {
		
		String jsonCallbackFunction =
			this.replaceURL(
				(httpRequest != null ? httpRequest.parameter("callback") : "")
			);
		
		FuseBaseRESTAPIMethod fuseBaseRESTAPIMethod =
			this.httpEndpointMethods.get(
				aPIPath
			);
		
		if(fuseBaseRESTAPIMethod != null) {
			
			Object[] methodParameters =
				new Object[]{
					new FuseBaseRESTAPICall(
						jsonCallbackFunction,
						output,
						httpRequest,
						httpServerThread
					)
				};
			
			if(fuseBaseRESTAPIMethod.requiresAuthentication()) {
				
				String sessionKey =
					this.getSessionKey(
						httpRequest
					);
				
				if(this.isAuthenticated(sessionKey, httpServerThread)) {
					
					if(this.isAuthorized(sessionKey, fuseBaseRESTAPIMethod.apiPath())) {
						
						fuseBaseRESTAPIMethod.method().invoke(
							this,
							methodParameters
						);
						
					} else {
						
						this.apiResponseError(
							output,
							"Not authorized.",
							jsonCallbackFunction
						);
						
					}
					
				} else {
					
					this.apiResponseError(
						output,
						"Authentication required.",
						jsonCallbackFunction
					);
					
				}
				
			} else {
				
				fuseBaseRESTAPIMethod.method().invoke(
					this,
					methodParameters
				);
				
			}
			
			return true;
			
		}
		
		return false;
		
	}
	
	private String getSessionKey(HttpRequest httpRequest) {
		
		//System.out.println(httpRequest.toString());
		
		if(httpRequest == null) {
			return null;
		}
		
		String sessionKey =
			httpRequest.parameter(Config.FUSEBASE_SESSION_KEY_COOKIE_KEYNAME);
			
		if(sessionKey == null) {
			
			sessionKey =
				httpRequest.cookie(Config.FUSEBASE_SESSION_KEY_PARAMETER_KEYNAME);
			
		}
		
		return sessionKey;
		
	}
	
	private boolean isAuthenticated(String sessionKey, HTTPServerThread httpServerThread) {
		
		if(sessionKey != null) {
			
			if(FuseBase.userManager.isValidSession(sessionKey, httpServerThread.getClientIPAddress())) {
				
				return true;
				
			} else {
				
				return false;
				
			}
			
		}
		
		return false;
	}
	
	private boolean isAuthenticated(HttpRequest httpRequest, HTTPServerThread httpServerThread) {
		
		String sessionKey =
			this.getSessionKey(httpRequest);
		
		return this.isAuthenticated(sessionKey, httpServerThread);
		
	}
	
	private boolean isAuthorized(String sessionKey, String privilegeKey) {
		
		if(sessionKey != null) {
			
			UserSession userSession =
				FuseBase.userManager.getUserSession(sessionKey);
			
			if(userSession != null) {
				
				return userSession.user().isAuthorized(privilegeKey);
				
			} else {
				
				return false;
				
			}
			
		}
		
		return false;
		
	}
	
	private String getFuseBaseCookie(String sessionKey, String hostName) {
		
		return
			Config.FUSEBASE_SESSION_KEY_COOKIE_KEYNAME + "=" + sessionKey +
			"; Path=/; domain=" + hostName;
		
	}
	
	public String getFuseBaseCookieForRemoval(String hostName) {
		
		return
			Config.FUSEBASE_SESSION_KEY_COOKIE_KEYNAME + 
			"=not_valid; Path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT; domain=" + 
			hostName;
		
	}
	
	/*
	** API mapper methods below
	*/
	
	@HTTP_ENDPOINT
	public void json_parse(FuseBaseRESTAPICall c) throws Exception {
		
		this.json_parse(
			this.replaceURL(c.httpRequest().parameter("unparsedjson")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void json_parse(		@Parameter(name="unparsedjson", required=false)
								String unparsedJson,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		try {
			
			JSONParser jsonParser =
				new JSONParser(unparsedJson);
				
			this.jsonReply(
				c.output(),
				jsonParser.toString(),
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void sql_parse(FuseBaseRESTAPICall c) throws Exception {
		
		this.sql_parse(
			this.replaceURL(c.httpRequest().parameter("sql")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void sql_parse(		@Parameter(name="sql", required=false)
								String sql,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		try {
			
			SQLParser sqlParser = new SQLParser(sql);
				
			this.jsonReply(
				c.output(),
				sqlParser.toJSON(),
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT(requiresAuthentication=false)
	public void clientkey_authenticate(FuseBaseRESTAPICall c) throws Exception {
		
		this.clientkey_authenticate(
			this.replaceURL(c.httpRequest().parameter("clientKey")),
			Long.parseLong(c.httpRequest().parameter("sessionExpiryTimeInMs", "36000000")),
			OutputType.valueOf(
				this.replaceURL(
					c.httpRequest().parameter(
						"outputType",
						OutputType.DEFAULT_VALUE.toString()
					)
				)
			),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET, requiresAuthentication=false)
	public void clientkey_authenticate(	@Parameter(name="clientKey")
										String clientKey,
										@Parameter(name="sessionExpiryTimeInMs")
										long sessionExpiryTimeInMs,
										@Parameter(name="outputType", required=false)
										OutputType outputType,
										FuseBaseRESTAPICall c
					
									) throws Exception {
		
		String sessionKey =
			this.FuseBase.userManager.authenticateClientKey(
				clientKey,
				c.httpServerThread().getClientIPAddress(),
				sessionExpiryTimeInMs
			);
		
		if(sessionKey != null) {
			
			if(outputType != OutputType.CSV) {
				
				this.jsonReply(
					c.output(),
					JSONBuilder.f().$('{').k("sessionKey").v(sessionKey).$('}'),
					c.jsonCallbackFunction()
				);
				
			} else if(outputType == OutputType.CSV) {
				
				HttpResponse.header(
					c.output(),
					HttpResponse.CSV_HEADERS
				);
				
				c.output().writeBytes(
					"SessionKey\n" +
					sessionKey
				);
				
			}
		
		} else if(sessionKey == null) {
			
			String feedback =
				"Failed to authenticate client key.";
			
			if(outputType != OutputType.CSV) {
				
				this.apiResponseError(
					c.output(),
					feedback,
					c.jsonCallbackFunction()
				);
				
			} else if(outputType == OutputType.CSV) {
				
				HttpResponse.header(
					c.output(),
					HttpResponse.CSV_HEADERS
				);
				
				c.output().writeBytes(
					"SessionKey\n" +
					feedback
				);
				
			}
		
		}
		
	}
	
	@HTTP_ENDPOINT(requiresAuthentication=false)
	public void user_authenticate(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_authenticate(
			this.replaceURL(c.httpRequest().parameter("h")),
			Long.parseLong(c.httpRequest().parameter("sessionExpiryTimeInMs")),
			this.replaceURL(c.httpRequest().parameter("hostName")),
			c
		);
		
	}
	
	/*
	Testing:
		"http://fuse.cisco.com:4444/user/authenticate?h=" + window.btoa('admin' + ':' + 'peregrine') + "&sessionExpiryTimeInMs=3600000"
	*/
	@API(type=HttpRequestType.GET, requiresAuthentication=false)
	public void user_authenticate(	@Parameter(name="h")
									String base64EncodedUsernameAndPassword,
									@Parameter(name="sessionExpiryTimeInMs")
									long sessionExpiryTimeInMs,
									@Parameter(name="hostname")
									String hostName,
									FuseBaseRESTAPICall c
					
								) throws Exception {
		try {
			
			String sessionKey =
				this.FuseBase.userManager.authenticateUser(
					base64EncodedUsernameAndPassword,
					c.httpServerThread().getClientIPAddress(),
					sessionExpiryTimeInMs
				);

			if(sessionKey != null) {
				
				// Signal success and set session cookie at client
				
				this.jsonReply(
					c.output(),
					JSONBuilder.f().$('{').k("status").v("success").$('}'),
					c.jsonCallbackFunction(),
					this.getFuseBaseCookie(sessionKey, hostName)
				);

			} else if(sessionKey == null) {
				
				this.jsonReply(
					c.output(),
					JSONBuilder.f().$('{').k("status").v("failure").k("message").v("Wrong username or password.").$('}'),
					c.jsonCallbackFunction()
				);

			}
			
		} catch (Exception e) {
			
			this.jsonReply(
				c.output(),
				JSONBuilder.f().$('{').k("status").v("failure").k("message").v("Missing or wrong input parameters.").$('}'),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT(requiresAuthentication=false)
	public void user_logout(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_logout(c.jsonCallbackFunction(), c);
		
	}
	
	@API(type=HttpRequestType.GET, requiresAuthentication=false)
	public void user_logout(	@Parameter(name="jsonCallbackFunction", required=false) String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		String sessionKey =
			this.getSessionKey(c.httpRequest());
			
		this.FuseBase.userManager.deleteUserSession(sessionKey);
			
		HttpResponse.redirect(
			c.output(),
			Config.FUSEBASE_WEB_LOGIN_PATH,
			this.getFuseBaseCookieForRemoval(c.httpServerThread().getClientHostName())
		);
		
	}
	
	@HTTP_ENDPOINT(requiresAuthentication=false)
	public void user_current(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_current(c.jsonCallbackFunction(), c);
		
	}
	
	@API(type=HttpRequestType.GET, requiresAuthentication=false)
	public void user_current(	@Parameter(name="jsonCallbackFunction", required=false) String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		String sessionKey =
			this.getSessionKey(c.httpRequest());
			
		UserSession userSession =
			this.FuseBase.userManager.getUserSession(sessionKey);
			
		if(userSession != null) {
			
			this.jsonReply(
				c.output(),
				JSONBuilder.f().
					$('[').$('{').
						k("username").v(userSession.user().username()).
					$('}').$(']'),
				c.jsonCallbackFunction()
			);
			
		} else if(userSession == null) {
			
			this.apiResponseError(
				c.output(),
				"Not logged in.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_add(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_add(
			this.replaceURL(c.httpRequest().parameter("h")),
			this.replaceURL(c.httpRequest().parameter("roleList", "")),
			this.replaceURL(c.httpRequest().parameter("privilegeList", "")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_add(	@Parameter(name="h")
							String base64EncodedUsernameAndPassword,
							@Parameter(name="roleList")
							String roleList,
							@Parameter(name="privilegeList")
							String privilegeList,
							@Parameter(name="jsonCallbackFunction", required=false)
							String jsonCallbackFunction,
							FuseBaseRESTAPICall c
							
						) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addUser(
				base64EncodedUsernameAndPassword,
				(roleList == null || roleList.equals("") ? new String[]{} : roleList.split(",")),
				(privilegeList == null || privilegeList.equals("") ? new String[]{} : privilegeList.split(","))
			);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added user.",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to add user. Either a user with the same username already exists or your input is wrong. Neither username or password can contain the character ':'.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_delete(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_delete(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_delete(	@Parameter(name="username")
								String username,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteUser(
				username
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully delete user and all its sessions, client keys and privileges.",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NOT_ALLOWED) {
			
			this.apiResponseError(
				c.output(),
				"User cannot be deleted.",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NOT_ALLOWED) {
			
			this.apiResponseError(
				c.output(),
				"User does not exist.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_changepassword(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_changepassword(
			this.replaceURL(c.httpRequest().parameter("h1")),
			this.replaceURL(c.httpRequest().parameter("h2")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_changepassword(	@Parameter(name="h1")
										String base64EncodedUsernameAndOldPassword,
										@Parameter(name="h2")
										String base64EncodedUsernameAndNewPassword,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
									) throws Exception {
		
		UserManagerFeedback feedback =
			this.FuseBase.userManager.changeUserPassword(
				base64EncodedUsernameAndOldPassword,
				base64EncodedUsernameAndNewPassword
			);
			
		if(feedback == UserManagerFeedback.PASSWORD_CHANGED) {
			
			this.apiResponseSuccess(
				c.output(),
				feedback.toString(),
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				feedback.toString(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void api(FuseBaseRESTAPICall c) throws Exception {
		
		this.api(c.jsonCallbackFunction(), c);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void api(	@Parameter(name="jsonCallbackFunction", required=false) String jsonCallbackFunction,
						FuseBaseRESTAPICall c
					
					) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.apiMethodDescriptor.toString(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void connections_list(FuseBaseRESTAPICall c) throws Exception {
													
		this.connections_list(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_list(	@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c	
									
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			FuseBase.dbConnectionManager.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void connections_addorupdate(FuseBaseRESTAPICall c) throws Exception {
		
		String connectionName = replaceURL(c.httpRequest().parameter("connectionName")),
			connectString = replaceURL(c.httpRequest().parameter("connectString")),
			user = replaceURL(c.httpRequest().parameter("user")),
			passWord = replaceURL(c.httpRequest().parameter("passWord")),
			jdbcDriverClass = replaceURL(c.httpRequest().parameter("jdbcDriverClass")),
			jdbcDriverInfoName = replaceURL(c.httpRequest().parameter("jdbcDriverInfoName"));
		
		this.connections_addorupdate(
			connectionName,
			connectString,
			user,
			passWord,
			jdbcDriverClass,
			jdbcDriverInfoName,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_addorupdate(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
											String connectionName,
											@Parameter(name="connectString")
											String connectString,
											@Parameter(name="user")
											String user,
											@Parameter(name="passWord")
											String passWord,
											@Parameter(name="jdbcDriverClass")
											String jdbcDriverClass,
											@Parameter(name="jdbcDriverInfoName")
											String jdbcDriverInfoName,
											@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
										
										) throws Exception {
		
		MapAction mapAction =
			FuseBase.dbConnectionManager.addOrUpdateConnection(
				connectionName,
				connectString,
				user,
				passWord,
				jdbcDriverClass,
				jdbcDriverInfoName
			);
		
		if(mapAction == MapAction.ADD) {
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully added connection \"" + connectionName + "\".",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.UPDATE) { // Update connection
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully updated connection \"" + connectionName + "\".",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_delete(FuseBaseRESTAPICall c) throws Exception {
		
		String connectionName =
			replaceURL(c.httpRequest().parameter("connectionName"));
		
		this.connections_delete(
			connectionName,
			c.jsonCallbackFunction(),
			c
		);
	
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_delete(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
									String connectionName,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
								
									) throws Exception {
		
		MapAction mapAction =
			FuseBase.dbConnectionManager.deleteConnection(connectionName);
		
		if(mapAction == MapAction.DELETE) {
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted connection \"" + connectionName + "\".",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.NONE) {
		
			this.apiResponseError(
				c.output(),
				"Connection \"" + connectionName + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_connect(FuseBaseRESTAPICall c) throws Exception {
		
		String connectionName =
			replaceURL(c.httpRequest().parameter("connectionName"));
		
		this.connections_connect(
			connectionName,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_connect(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
										String connectionName,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
									
									) throws Exception {
	
		try {
		
			if(FuseBase.dbConnectionManager.connect(connectionName)) {
		
				this.apiResponseSuccess(
					c.output(),
					"Successfully connected connection \"" + connectionName + "\".",
					c.jsonCallbackFunction()
				);
		
			} else {
				
				this.apiResponseError(
					c.output(),
					"Connection \"" + connectionName + "\" does not exist.",
					c.jsonCallbackFunction()
				);
		
			}
		
		} catch(Exception e) {
			
			if(e instanceof SQLException) {
				
				e = (SQLException)e;

			} else if(e instanceof NoSuchElementException) {
				
				e = (NoSuchElementException)e;
				
			}
			
			this.apiResponseError(
				c.output(),
				"Failed to connect to \"" + connectionName + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
		
			return;
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_setdefault(FuseBaseRESTAPICall c) throws Exception {
		
		String connectionName =
			replaceURL(c.httpRequest().parameter("connectionName"));
		
		this.connections_setdefault(
			connectionName,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_setdefault(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
										String connectionName,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
										
										) throws Exception {
		
		if(FuseBase.dbConnectionManager.setDefaultConnection(connectionName)) {
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully set \"" + connectionName + "\" as default connection!",
				c.jsonCallbackFunction()
			);
		
		} else {
		
			this.apiResponseError(
				c.output(),
				"Connection \"" + connectionName + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_registeredqueries(FuseBaseRESTAPICall c) throws Exception {
		
		this.connections_registeredqueries(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_registeredqueries(	@Parameter(name="jsonCallbackFunction", required=false)
												String jsonCallbackFunction,
												FuseBaseRESTAPICall c
												
												) throws Exception {
		
		this.jsonReply(
			c.output(),
			FuseBase.queryManager.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void connections_jdbcdriverinfo(FuseBaseRESTAPICall c) throws Exception {
		
		this.connections_jdbcdriverinfo(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_jdbcdriverinfo(	@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
											
											) throws Exception {
		
		this.jsonReply(
			c.output(),
			JDBCDriverInfo.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void connections_querytypes(FuseBaseRESTAPICall c) throws Exception {
		
		this.connections_querytypes(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_querytypes(	@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
											
											) throws Exception {
		
		this.jsonReply(
			c.output(),
			QueryType.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void connections_query(FuseBaseRESTAPICall c) throws Exception {
		
		String
			connectionName = c.httpRequest().parameter("connectionName"),
			query = this.replaceURL(c.httpRequest().parameter("query")),
			queryId = c.httpRequest().parameter("queryId");
		
		QueryType queryType =
			QueryType.valueOf(
				c.httpRequest().parameter(
					"queryType",
					QueryType.DEFAULT_VALUE.toString()
				)
			);
			
		OutputType outputType =
			OutputType.valueOf(
				c.httpRequest().parameter(
					"outputType",
					OutputType.DEFAULT_VALUE.toString()
				)
			);
		
		this.connections_query(
			connectionName,
			query,
			queryType,
			outputType,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_query(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
									String connectionName,
									@Parameter(name="query")
									String query,
									@Parameter(name="queryType", required=false)
									QueryType queryType,
									@Parameter(name="outputType", required=false)
									OutputType outputType,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
									
									) throws Exception {
		
		this.queryConnection(
			connectionName,
			query,
			queryType,
			outputType,
			jsonCallbackFunction,
			c
		);
	
	}
	
	@HTTP_ENDPOINT
	public void connections_registeredquery(FuseBaseRESTAPICall c) throws Exception {
		
		String
			connectionName = c.httpRequest().parameter("connectionName"),
			queryId = c.httpRequest().parameter("queryId");
		
		QueryType queryType =
			QueryType.valueOf(
				c.httpRequest().parameter(
					"queryType",
					QueryType.DEFAULT_VALUE.toString()
				)
			);
			
		OutputType outputType =
			OutputType.valueOf(
				c.httpRequest().parameter(
					"outputType",
					OutputType.DEFAULT_VALUE.toString()
				)
			);
		
		this.connections_registeredquery(
			connectionName,
			queryId,
			queryType,
			outputType,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_registeredquery(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
												String connectionName,
												@Parameter(name="queryId")
												String queryId,
												@Parameter(name="queryType", required=false)
												QueryType queryType,
												@Parameter(name="outputType", required=false)
												OutputType outputType,
												@Parameter(name="jsonCallbackFunction", required=false)
												String jsonCallbackFunction,
												FuseBaseRESTAPICall c
											
											) throws Exception {
		
		Query query = null;
		
		if(queryId != null) {
		
			if(!FuseBase.queryManager.hasQuery(queryId)) {
		
				this.apiResponseError(
					c.output(),
					"Query Id \"" + queryId + "\" does not exist.",
					c.jsonCallbackFunction()
				);
		
				return;
		
			} else {
		
				query =
					FuseBase.queryManager.getQuery(queryId);
					
				this.queryConnection(
					(connectionName == null ? query.getConnectionName() : connectionName),
					query.getQuery(),
					queryType,
					outputType,
					jsonCallbackFunction,
					c
				);
		
			}
		} else if(queryId == null) {
			
			this.apiResponseError(
				c.output(),
				"No Query Id provided.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	// Helper method for querying
	private void queryConnection(	String connectionName,
									String query,
									QueryType queryType,
									OutputType outputType,
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c			) throws Exception {
		
		if(!FuseBase.dbConnectionManager.hasConnection(connectionName)) {
		
			this.apiResponseError(
				c.output(),
				"Connection \"" + connectionName + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
			return;
		
		}
		
		if(query != null && !query.equals("")) {
			for(String key : c.httpRequest().getParameterKeys()) {
				if(key.substring(0,1).equals(":")) {
					if(c.httpRequest().parameter(key) != null) {
						query = query.replaceAll(key, c.httpRequest().parameter(key));
					}
				}
			}
		}
		
		if(query == null) {
		
			this.apiResponseError(
				c.output(),
				"No query provided.",
				c.jsonCallbackFunction()
			);
		
			return;
		
		}
		
		if(queryType == QueryType.SQL) {
		
			DataWriter dataWriter = null;
			PrintWriter printOutput = new PrintWriter(c.output());
			String[] headers = HttpResponse.JSON_HEADERS;
			
			DataWriterType dataWriterType =
				DataWriterType.valueOf(
					outputType.toString()
				);
		
			switch(dataWriterType) {
				case JSON:
					dataWriter = new JSONWriter(printOutput);
					break;
				case JSON_TABULAR:
					dataWriter = new JSONWriterTabular(printOutput);
					break;
				case CSV:
					dataWriter = new CSVWriter(printOutput);
					headers = HttpResponse.CSV_HEADERS;
					break;
				default:
					break;
			}
		
			try {
		
				QueryObject queryObject =
					FuseBase.queryManager.getQueryObject(
						connectionName,
						query
					);
		
				HttpResponse.header(
					c.output(),
					headers
				);
		
				if(	(dataWriterType == DataWriterType.JSON || dataWriterType == DataWriterType.JSON_TABULAR) &&
					c.jsonCallbackFunction() != null	) {
		
					printOutput.print(c.jsonCallbackFunction() + "(");
		
				}
		
				FuseBase.queryManager.printResultSet(
					queryObject,
					dataWriter
				);
		
				if(	(dataWriterType == DataWriterType.JSON || dataWriterType == DataWriterType.JSON_TABULAR) &&
					c.jsonCallbackFunction() != null	) {
		
					printOutput.print(");");
		
				}
		
				printOutput.flush();
		
			} catch(Exception e) {
		
				this.apiResponseError(
					c.output(),
					"Failed to execute query using connection \"" + connectionName + "\"." +
						(e.getMessage() != null ? " " + e.getMessage() : ""),
					c.jsonCallbackFunction()
				);
		
				return;
		
			}
		
		} else if(queryType == QueryType.DML) {
		
			try {
		
				int recordCount =
					FuseBase.queryManager.dml(
						connectionName,
						query
					);
		
				this.apiResponseSuccess(
					c.output(),
					recordCount + " rows affected.",
					c.jsonCallbackFunction()
				);
		
			} catch(Exception e) {
		
				this.apiResponseError(
					c.output(),
					e.getMessage(),
					c.jsonCallbackFunction()
				);
		
				return;
		
			}
		
		} else if(queryType == QueryType.DDL) {
		
			try {
		
				int ddlStatus =
					FuseBase.queryManager.dml(
						connectionName,
						query
					);
		
				this.apiResponseSuccess(
					c.output(),
					"DDL status: " + ddlStatus,
					c.jsonCallbackFunction()
				);
		
			} catch(Exception e) {
		
				this.apiResponseError(
					c.output(),
					e.getMessage(),
					c.jsonCallbackFunction()
				);
		
				return;
		
			}
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_query_register(FuseBaseRESTAPICall c) throws Exception {
		
		String
			connectionName = c.httpRequest().parameter("connectionName"),
			query = this.replaceURL(c.httpRequest().parameter("query")),
			queryId = c.httpRequest().parameter("queryId");
		
		this.connections_query_register(
			connectionName,
			query,
			queryId,
			c.jsonCallbackFunction(),
			c
		);
	
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_query_register(	@Parameter(name="connectionName", validValuesClass=DBConnectionManager.class)
											String connectionName,
											@Parameter(name="query")
											String query,
											@Parameter(name="queryId")
											String queryId,
											@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
											
											) throws Exception {
		
		MapAction mapAction =
			FuseBase.queryManager.addOrUpdateQuery(
				queryId,
				query,
				connectionName
			);
		
		if(mapAction == MapAction.ADD) {
		
			this.apiResponseSuccess(
				c.output(),
				"Query Id \"" + queryId + "\" has been registered.",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.UPDATE) {
		
			this.apiResponseSuccess(
				c.output(),
				"Query Id \"" + queryId + "\" has been updated.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void connections_query_delete(FuseBaseRESTAPICall c) throws Exception {
		
		String queryId = c.httpRequest().parameter("queryId");
		
		this.connections_query_delete(
			queryId,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void connections_query_delete(	@Parameter(name="queryId", validValuesClass=QueryManager.class)
											String queryId,
											@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
										
										) throws Exception {
		
		MapAction mapAction =
			FuseBase.queryManager.deleteQuery(queryId);
		
		if(mapAction == MapAction.DELETE) {
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully removed Query Id \"" + queryId + "\".",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.NONE) {
		
			this.apiResponseError(
				c.output(),
				"Query Id \"" + queryId + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void files_directorylisting(FuseBaseRESTAPICall c) throws Exception {
		
		String path =
			this.replaceURL(
				c.httpRequest().parameter("path")
			);
		
		this.files_directorylisting(
			c.jsonCallbackFunction(),
			path,
			c
		);

	}
	
	@API(type=HttpRequestType.GET)
	public void files_directorylisting(	@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										@Parameter(name="path")
										String path,
										FuseBaseRESTAPICall c
										
										) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.fileManager.listFullDirectory(path),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void apps(FuseBaseRESTAPICall c) throws Exception {
		
		this.apps(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void apps(	@Parameter(name="jsonCallbackFunction", required=false)
						String jsonCallbackFunction,
						FuseBaseRESTAPICall c
					
					) throws Exception {
	
		this.jsonReply(
			c.output(),
			this.FuseBase.fileManager.listFullDirectory("./" + Config.APPS_DIRECTORY + "/"),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void apps_upload(FuseBaseRESTAPICall c) throws Exception {
		
		String
			fileName = replaceURL(c.httpRequest().parameter("fileName")),
			fileContents = new String(c.httpRequest().getClientData(), StandardCharsets.UTF_8);
			
		this.apps_upload(
			fileName,
			fileContents,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.POST)
	public void apps_upload(	@Parameter(name="fileName")
								String fileName,
								@Parameter(name="data")
								String fileContents,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		if(fileName != null && c.httpRequest().getFileName().length() > 0) {
		
			if(fileName.substring(1, 1).equals("/")) {
				fileName = "./" + Config.APPS_DIRECTORY + fileName;
			} else {
				fileName = "./" + Config.APPS_DIRECTORY + "/" + fileName;
			}
		
			this.FuseBase.fileManager.writeToFile(
				fileName,
				fileContents
			);
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully uploaded filename \"" + fileName + "\".",
				c.jsonCallbackFunction()
			);
		
		} else {
		
			this.apiResponseError(
				c.output(),
				"No filename or file contents provided.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void file_upload(FuseBaseRESTAPICall c) throws Exception {
			
		this.file_upload(
			replaceURL(c.httpRequest().parameter("fileName")),
			c.httpRequest().getClientData(),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.POST)
	public void file_upload(	@Parameter(name="fileName")
								String fileName,
								@Parameter(name="data")
								byte[] fileContents,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		if(fileName != null && c.httpRequest().getFileName().length() > 0) {
		
			/*if(fileName.substring(1, 1).equals("/")) {
				fileName = "./" + Config.APPS_DIRECTORY + fileName;
			} else {
				fileName = "./" + Config.APPS_DIRECTORY + "/" + fileName;
			}*/
				
			if(fileName.substring(1, 1).equals("/")) {
				fileName = "." + fileName;
			} else {
				fileName = "./" + fileName;
			}
				
			this.FuseBase.fileManager.writeToBinaryFile(
				fileName,
				fileContents
			);
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully uploaded filename \"" + fileName + "\".",
				c.jsonCallbackFunction()
			);
		
		} else {
		
			this.apiResponseError(
				c.output(),
				"No filename or file contents provided.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scripts(FuseBaseRESTAPICall c) throws Exception {
		
		this.scripts(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scripts(	@Parameter(name="jsonCallbackFunction", required=false)
							String jsonCallbackFunction,
							FuseBaseRESTAPICall c
						
						) throws Exception {
		
		this.jsonReply(
			c.output(),
			FuseBase.scriptManager.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void scripts_source(FuseBaseRESTAPICall c) throws Exception {
		
		String name = replaceURL(c.httpRequest().parameter("name"));
		
		this.scripts_source(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scripts_source(
							@Parameter(name="name")
							String name,
							@Parameter(name="jsonCallbackFunction", required=false)
							String jsonCallbackFunction,
							FuseBaseRESTAPICall c
						
						) throws Exception {
		
		this.textReply(
			c.output(),
			FuseBase.scriptManager.getScript(name).source()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void scripts_upload(FuseBaseRESTAPICall c) throws Exception {
		
		String name = replaceURL(c.httpRequest().parameter("name"));
		
		this.scripts_upload(
			name,
			new String(c.httpRequest().getClientData()),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.POST)
	public void scripts_upload(	@Parameter(name="name")
								String name,
								@Parameter(name="source")
								String source,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
								
								) throws Exception {
		
		MapAction mapAction =
			FuseBase.scriptManager.addOrUpdateScript(
				name,
				source
			);
		
		if(mapAction == MapAction.ADD) {
		
			this.apiResponseSuccess(
				c.output(),
				"Added script \"" + name + "\".",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.UPDATE) {
		
			this.apiResponseSuccess(
				c.output(),
				"Updated script \"" + name + "\".",
				c.jsonCallbackFunction()
			);
		
		}
	
	}
	
	@HTTP_ENDPOINT
	public void scripts_delete(FuseBaseRESTAPICall c) throws Exception {
		
		String name = replaceURL(c.httpRequest().parameter("name"));
		
		this.scripts_delete(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scripts_delete(	@Parameter(name="name", validValuesClass=ScriptManager.class)
								String name,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		MapAction mapAction =
			FuseBase.scriptManager.deleteScript(name);
		
		if(mapAction == MapAction.DELETE) {
		
			this.apiResponseSuccess(
				c.output(),
				"Successfully removed script \"" + name + "\".",
				c.jsonCallbackFunction()
			);
		
		} else if(mapAction == MapAction.NONE) {
		
			this.apiResponseError(
				c.output(),
				"Script \"" + name + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scripts_run(FuseBaseRESTAPICall c) throws Exception {
		
		String name = replaceURL(c.httpRequest().parameter("name"));
		
		this.scripts_run(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scripts_run(	@Parameter(name="name", validValuesClass=ScriptManager.class)
								String name,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
							) throws Exception {
		
		Script script =
			FuseBase.scriptManager.getScript(name);
			
		if(script != null) {
		
			try {
		
				script.init(
					this.FuseBase.scriptAPI()
				);
		
				script.addBinding(
					"parameters",
					c.httpRequest().getParameters()
				);
				
				script.addBinding(
					"cookies",
					c.httpRequest().getCookies()
				);
		
				PersistedDataOutputStream persistedOutput =
					new PersistedDataOutputStream();
		
				script.run(
					persistedOutput
				);
				
				byte[] bytes = persistedOutput.getData().getBytes("UTF-8");
		
				c.output().write(bytes, 0, bytes.length);
		
				c.output().flush();
		
			} catch(Exception e) {
		
				this.apiResponseError(
					c.output(),
					"Failed to run script \"" + name + "\". Error message: \"" + JSONBuilder.jsonEscape(e.getMessage()) + "\".",
					c.jsonCallbackFunction()
				);
		
			}
		
		
		} else {
		
			this.apiResponseError(
				c.output(),
				"Script \"" + name + "\" does not exist.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scripts_run_anonymous(FuseBaseRESTAPICall c) throws Exception {
		
		String scriptSource = replaceURL(c.httpRequest().parameter("source"));
		
		this.scripts_run_anonymous(
			new String(c.httpRequest().getClientData()),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.POST)
	public void scripts_run_anonymous(	@Parameter(name="source")
										String scriptSource,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
									) throws Exception {
			
		if(scriptSource != null && scriptSource.length() > 0) {
		
			try {
				
				PersistedDataOutputStream persistedOutput =
					new PersistedDataOutputStream();
				
				Script.runAnonymousScript(
					this.FuseBase.scriptAPI(),
					c.httpRequest().getParameters(),
					c.httpRequest().getCookies(),
					persistedOutput,
					scriptSource
				);
				
				byte[] bytes = persistedOutput.getData().getBytes("UTF-8");
		
				c.output().write(bytes, 0, bytes.length);
		
				c.output().flush();
		
			} catch(Exception e) {
				
				e.printStackTrace();
		
				this.apiResponseError(
					c.output(),
					"Failed to run script. Error message: \"" + JSONBuilder.jsonEscape(e.getMessage()) + "\".",
					c.jsonCallbackFunction()
				);
		
			}
		
		
		} else {
		
			this.apiResponseError(
				c.output(),
				"No script provided.",
				c.jsonCallbackFunction()
			);
		
		}
		
	}
	
	@HTTP_ENDPOINT
	public void users(FuseBaseRESTAPICall c) throws Exception {
		
		this.users(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users(	@Parameter(name="jsonCallbackFunction", required=false)
						String jsonCallbackFunction,
						FuseBaseRESTAPICall c
							
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.userManager.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_sessions_deleteall(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_sessions_deleteall(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_sessions_deleteall(	@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
							
										) throws Exception {
		
		this.FuseBase.userManager.deleteAllUserSessions();
		
		this.apiResponseSuccess(
			c.output(),
			"Deleted all user sessions.",
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_sessions_removeexpired(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_sessions_removeexpired(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_sessions_removeexpired(	@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
							
										) throws Exception {
		
		this.FuseBase.userManager.removeExpiredSessions();
		
		this.apiResponseSuccess(
			c.output(),
			"Removed all expired user sessions.",
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_sessions(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_sessions(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_sessions(	@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.userManager.getUserSessionsJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles(	@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.userManager.getRolesJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_types(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_types(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_types(	@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
									) throws Exception {
		
		this.jsonReply(
			c.output(),
			RoleType.getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_add(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_add(
			c.httpRequest().parameter("name"),
			RoleType.valueOf(c.httpRequest().parameter("roleType")),
			this.replaceURL(c.httpRequest().parameter("privilegeList")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_add(	@Parameter(name="name")
									String name,
									@Parameter(name="roleType")
									RoleType roleType,
									@Parameter(name="privilegeList")
									String privilegeList,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
									) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addRole(
				name,
				roleType,
				privilegeList.split(",")
			);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added role \"" + name + "\" with privileges \"" + privilegeList + "\" .",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to add role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_delete(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_delete(
			c.httpRequest().parameter("name"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_delete(	@Parameter(name="name")
									String name,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
									) throws Exception {

		MapAction mapAction =
			this.FuseBase.userManager.deleteRole(
				name
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_delete_all(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_delete_all(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_delete_all(	@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
										) throws Exception {

		MapAction mapAction =
			this.FuseBase.userManager.deleteAllRoles();
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted all deleteable roles.",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete all roles.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_update_privileges(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_update_privileges(
			c.httpRequest().parameter("name"),
			this.replaceURL(c.httpRequest().parameter("privilegeList", "")),
			this.replaceURL(c.httpRequest().parameter("privilegeListToRemove", "")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_update_privileges(	@Parameter(name="name")
												String name,
												@Parameter(name="privilegeList")
												String privilegeList,
												@Parameter(name="privilegeListToRemove")
												String privilegeListToRemove,
												@Parameter(name="jsonCallbackFunction", required=false)
												String jsonCallbackFunction,
												FuseBaseRESTAPICall c
							
											) throws Exception {
		
		this.FuseBase.userManager.deleteRolePrivileges(
			name,
			privilegeListToRemove.split(",")
		);
		
		MapAction mapAction =
			this.FuseBase.userManager.addRolePrivileges(
				name,
				privilegeList.split(",")
			);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added privileges \"" + privilegeList + "\" and removed \"" + privilegeListToRemove + "\" for role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to add privileges to role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void users_roles_delete_privileges(FuseBaseRESTAPICall c) throws Exception {
		
		this.users_roles_delete_privileges(
			c.httpRequest().parameter("name"),
			c.httpRequest().parameter("privilegeList"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void users_roles_delete_privileges(	@Parameter(name="name")
												String name,
												@Parameter(name="privilegeList")
												String privilegeList,
												@Parameter(name="jsonCallbackFunction", required=false)
												String jsonCallbackFunction,
												FuseBaseRESTAPICall c
							
												) throws Exception {
		
		String[] privilegeArray =
			privilegeList.split(",");
			
		MapAction mapAction =
			this.FuseBase.userManager.deleteRolePrivileges(
				name,
				privilegeArray
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted privileges \"" + privilegeList + "\" from role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} else if(mapAction == MapAction.NONE) {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete privileges from role \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void privileges(FuseBaseRESTAPICall c) throws Exception {
		
		this.privileges(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void privileges(	@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.apiEndpointsJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void user_clientkeys(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_clientkeys(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_clientkeys(	@Parameter(name="username")
									String username,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
								) throws Exception {
		
		JSONBuilder jb =
			this.FuseBase.userManager.getUserClientKeys(username);
			
		if(jb != null) {
			
			this.jsonReply(
				c.output(),
				jb,
				c.jsonCallbackFunction()
			);
			
		} else if(jb == null) {
			
			this.apiResponseError(
				c.output(),
				"Username \"" + username + "\" does not exist.",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_generateclientkey(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_generateclientkey(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_generateclientkey(	@Parameter(name="username")
										String username,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
										) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addClientKey(username);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully generated new client key for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to generate new client key for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_clientkeys_add(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_clientkeys_add(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("clientKey"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_clientkeys_add(	@Parameter(name="username")
										String username,
										@Parameter(name="clientKey")
										String clientKey,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
										) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addExternalClientKey(username, clientKey);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added client key for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to add client key for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_clientkey_delete(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_clientkey_delete(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("clientkey"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_clientkey_delete(	@Parameter(name="username")
										String username,
										@Parameter(name="clientkey")
										String clientKey,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
									) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteClientKey(
				username,
				clientKey
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted client key \"" + clientKey + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete client key \"" + clientKey + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_clientkeys_deleteall(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_clientkeys_deleteall(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_clientkeys_deleteall(	@Parameter(name="username")
											String username,
											@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
							
										) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteUserClientKeys(username);
			
		if(mapAction == MapAction.DELETE_ALL) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted all client keys for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete all client keys for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_privileges(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_privileges(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_privileges(	@Parameter(name="username")
									String username,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
									) throws Exception {
		
		JSONBuilder jb =
			this.FuseBase.userManager.getUserRolesAndPrivilegesJSONBuilder(
				username
			);
			
		if(jb != null) {
			
			this.jsonReply(
				c.output(),
				jb,
				c.jsonCallbackFunction()
			);
			
		} else if(jb == null) {
			
			this.apiResponseError(
				c.output(),
				"Failed to list privileges for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_privileges_add(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_privileges_add(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("privilegeList"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_privileges_add(	@Parameter(name="username")
										String username,
										@Parameter(name="privilegeList")
										String privilegeList,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
									) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addUserPrivileges(
				username,
				privilegeList.split(",")
			);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added privileges \"" + privilegeList + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to add privileges for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_privileges_delete(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_privileges_delete(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("privilegeList"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_privileges_delete(	@Parameter(name="username")
										String username,
										@Parameter(name="privilegeList")
										String privilegeList,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
									) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteUserPrivileges(
				username,
				privilegeList.split(",")
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted privileges \"" + privilegeList + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete privileges for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_privileges_deleteall(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_privileges_deleteall(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_privileges_deleteall(	@Parameter(name="username")
											String username,
											@Parameter(name="jsonCallbackFunction", required=false)
											String jsonCallbackFunction,
											FuseBaseRESTAPICall c
							
										) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteAllUserPrivileges(
				username
			);
			
		if(mapAction == MapAction.DELETE_ALL) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted all privileges for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete all privileges for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_roles_add(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_roles_add(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("roleList"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_roles_add(	@Parameter(name="username")
								String username,
								@Parameter(name="roleList")
								String roleList,
								@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.addUserRoles(
				username,
				roleList.split(",")
			);
			
		if(mapAction == MapAction.ADD) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added roles \"" + roleList + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to add roles for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_roles_delete(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_roles_delete(
			c.httpRequest().parameter("username"),
			c.httpRequest().parameter("roleList"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_roles_delete(	@Parameter(name="username")
									String username,
									@Parameter(name="roleList")
									String roleList,
									@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c
							
									) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteUserRoles(
				username,
				roleList.split(",")
			);
			
		if(mapAction == MapAction.DELETE) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted roles \"" + roleList + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete roles for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_roles_deleteall(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_roles_deleteall(
			c.httpRequest().parameter("username"),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_roles_deleteall(	@Parameter(name="username")
										String username,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
							
										) throws Exception {
		
		MapAction mapAction =
			this.FuseBase.userManager.deleteAllUserRoles(
				username
			);
			
		if(mapAction == MapAction.DELETE_ALL) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully deleted all roles for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseError(
				c.output(),
				"Failed to delete all roles for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void user_privilegesandroles_update(FuseBaseRESTAPICall c) throws Exception {
		
		this.user_privilegesandroles_update(
			c.httpRequest().parameter("username"),
			this.replaceURL(c.httpRequest().parameter("privilegeList", "")),
			this.replaceURL(c.httpRequest().parameter("privilegeListToRemove", "")),
			this.replaceURL(c.httpRequest().parameter("roleList", "")),
			this.replaceURL(c.httpRequest().parameter("roleListToRemove", "")),
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void user_privilegesandroles_update(
	
			@Parameter(name="username")
			String username,
			@Parameter(name="priviligeList")
			String privilegeList,
			@Parameter(name="privilegeListToRemove")
			String privilegeListToRemove,
			@Parameter(name="roleList")
			String roleList,
			@Parameter(name="roleListToRemove")
			String roleListToRemove,
			@Parameter(name="jsonCallbackFunction", required=false)
			String jsonCallbackFunction,
			FuseBaseRESTAPICall c
			
		) throws Exception {
		
		MapAction[] mapActions =
			new MapAction[4];
		
		mapActions[0] =
			this.FuseBase.userManager.addUserPrivileges(
				username,
				privilegeList.split(",")
			);
		
		mapActions[1] =
			this.FuseBase.userManager.deleteUserPrivileges(
				username,
				privilegeListToRemove.split(",")
			);
		
		mapActions[2] =
			this.FuseBase.userManager.addUserRoles(
				username,
				roleList.split(",")
			);
		
		mapActions[3] =
			this.FuseBase.userManager.deleteUserRoles(
				username,
				roleListToRemove.split(",")
			);
		
		if(	mapActions[0] != MapAction.NONE ||
			mapActions[1] != MapAction.NONE ||
			mapActions[2] != MapAction.NONE ||
			mapActions[3] != MapAction.NONE	) {
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully added privileges \"" + privilegeList + "\", removed \"" + privilegeListToRemove + "\", " +
				"added roles \"" + roleList + "\" and removed \"" + roleListToRemove + "\" for username \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		} else {
			
			this.apiResponseSuccess(
				c.output(),
				"No changes to privileges and roles for \"" + username + "\".",
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs(FuseBaseRESTAPICall c) throws Exception {
		
		this.scheduler_jobs(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs(	@Parameter(name="jsonCallbackFunction", required=false)
								String jsonCallbackFunction,
								FuseBaseRESTAPICall c
							
								) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.scheduler().getJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs_create(FuseBaseRESTAPICall c) throws Exception {
		
		String
			name = this.replaceURL(c.httpRequest().parameter("name")),
			scriptName = c.httpRequest().parameter("scriptName");
		long
			initialDelay = Long.parseLong(c.httpRequest().parameter("initialDelay")),
			period = Long.parseLong(c.httpRequest().parameter("period"));
		
		TimeUnit timeUnit =
			TimeUnit.valueOf(
				c.httpRequest().parameter(
					"timeUnit",
					TimeUnit.MINUTES.toString()
				)
			);
		
		
		this.scheduler_jobs_create(
			name,
			scriptName,
			initialDelay,
			period,
			timeUnit,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs_create(	@Parameter(name="name")
										String name,
										@Parameter(name="scriptName", validValuesClass=ScriptManager.class)
										String scriptName,
										@Parameter(name="intitalDelay")
										long initialDelay,
										@Parameter(name="period")
										long period,
										@Parameter(name="timeUnit")
										TimeUnit timeUnit,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
									
									) throws Exception {
		
		Script script =
			this.FuseBase.scriptManager.getScript(scriptName);
		
		try {
			
			this.FuseBase.scheduler().scheduleJob(
				name,
				script,
				initialDelay,
				period,
				timeUnit
			);
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully created and scheduled job \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				"Failed to create and schedule job \"" + name + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_timeunits(FuseBaseRESTAPICall c) throws Exception {
		
		this.scheduler_timeunits(
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_timeunits(	@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
											
										) throws Exception {
		
		this.jsonReply(
			c.output(),
			this.FuseBase.scheduler().getTimeUnitJSONBuilder(),
			c.jsonCallbackFunction()
		);
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs_delete(FuseBaseRESTAPICall c) throws Exception {
		
		String
			name = this.replaceURL(c.httpRequest().parameter("name"));
		
		
		this.scheduler_jobs_delete(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs_delete(	@Parameter(name="name", validValuesClass=Scheduler.class)
										String name,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
									
									) throws Exception {
		
		try {
			
			this.FuseBase.scheduler().deleteJob(
				name
			);
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully stopped and deleted job \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				"Failed to stop and delete job \"" + name + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs_start(FuseBaseRESTAPICall c) throws Exception {
		
		String
			name = this.replaceURL(c.httpRequest().parameter("name"));
		
		
		this.scheduler_jobs_start(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs_start(	@Parameter(name="name", validValuesClass=Scheduler.class)
										String name,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
									
									) throws Exception {
		
		try {
			
			this.FuseBase.scheduler().startJob(
				name
			);
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully started schedule of job \"" + name + "\".",
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				"Failed to start schedule of job \"" + name + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs_logs(FuseBaseRESTAPICall c) throws Exception {
		
		String
			name = this.replaceURL(c.httpRequest().parameter("name"));
		
		
		this.scheduler_jobs_logs(
			name,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs_logs(	@Parameter(name="name", validValuesClass=Scheduler.class)
										String name,
										@Parameter(name="jsonCallbackFunction", required=false)
										String jsonCallbackFunction,
										FuseBaseRESTAPICall c
									
									) throws Exception {
		
		try {
			
			this.jsonReply(
				c.output(),
				this.FuseBase.fileManager.listFullDirectory(Scheduler.SCHEDULER_LOG_DIRECTORY + name),
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				"Failed list logs for job \"" + name + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void scheduler_jobs_logs_getfile(FuseBaseRESTAPICall c) throws Exception {
		
		String
			name = this.replaceURL(c.httpRequest().parameter("name")),
			fileName = this.replaceURL(c.httpRequest().parameter("fileName"));
		
		
		this.scheduler_jobs_logs_getfile(
			name,
			fileName,
			c.jsonCallbackFunction(),
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void scheduler_jobs_logs_getfile(	@Parameter(name="name", validValuesClass=Scheduler.class)
												String name,
												@Parameter(name="fileName")
												String fileName,
												@Parameter(name="jsonCallbackFunction", required=false)
												String jsonCallbackFunction,
												FuseBaseRESTAPICall c
											
											) throws Exception {
		
		try {
			
			String fullPath =
				this.FuseBase.scheduler().getFullPathOfSchedulerJobLogFile(
					name,
					fileName
				);
				
			if(fullPath != null) {
				
				HttpResponse.header(
					c.output(),
					HttpResponse.contentType(
						HttpResponse.getFileType(
							fileName
						)
					),
					-1
				);

				c.httpServerThread().serveFile(
					c.output(),
					fullPath
				);
				
			} else if(fullPath == null) {
				
				this.apiResponseError(
					c.output(),
					"Log file \"" + fileName + "\" for job \"" + name + "\" does not exist.",
					c.jsonCallbackFunction()
				);
				
			}
			
			
		} catch(Exception e) {
			
			this.apiResponseError(
				c.output(),
				"Failed to get log file \"" + fileName + "\" for job \"" + name + "\". Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void geturl(FuseBaseRESTAPICall c) throws Exception {
		
		String url =
			replaceURL(c.httpRequest().parameter("url"));
			
		this.geturl(
			url,
			c
		);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void geturl(	@Parameter(name="url") String url, FuseBaseRESTAPICall c) throws Exception {
		
		this.FuseBase.fileManager.printURL(
			c.output(),
			url
		);
		
	}
	
	@HTTP_ENDPOINT
	public void system_export_all(FuseBaseRESTAPICall c) throws Exception {
		
		this.system_export_all(c.jsonCallbackFunction(), c);
		
	}
	
	@API(type=HttpRequestType.GET)
	public void system_export_all(	@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c) throws Exception {
		try {
			
			byte[] exportData =
				this.FuseBase.exportManager.exportAll();

			HttpResponse.header(
				c.output(),
				new String[] {
					"Content-Type: application/octet-stream",
					"Content-Disposition: inline; filename=\"FuseBase.bak\""
				}
			);

			c.output().write(exportData, 0, exportData.length);
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	@HTTP_ENDPOINT
	public void system_import_all(FuseBaseRESTAPICall c) throws Exception {
		
		this.system_import_all(c.jsonCallbackFunction(), c);
		
	}
	
	@API(type=HttpRequestType.POST)
	public void system_import_all(	@Parameter(name="jsonCallbackFunction", required=false)
									String jsonCallbackFunction,
									FuseBaseRESTAPICall c) throws Exception {
		
		
			
		try {
			
			this.FuseBase.exportManager.importAll(
				c.httpRequest().getClientData()
			);
			
			this.apiResponseSuccess(
				c.output(),
				"Successfully imported FuseBase data.",
				c.jsonCallbackFunction()
			);
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
			this.apiResponseError(
				c.output(),
				"An error ocurred while importing FuseBase data. Error: " + e.getMessage(),
				c.jsonCallbackFunction()
			);
			
		}
		
		
	}
	
	/*
	** End of API mapper methods
	*/
	
	/*
	** Utility methods below
	*/
	
	public String replaceURL(String s) {
		
		try {
			
			return ud.decode(s, "UTF-8");
			
		} catch(Exception e) {
			
			;
			
		}
		
		return s;
	}
	
	private void apiResponseSuccess(DataOutputStream output, String message, String callback) throws Exception {
		
		this.jsonReply(
			output,
			JSONBuilder.f().
				$('[').
					$('{').
						k("status").v("OK").
						k("message").v(message).
					$('}').
				$(']'),
			callback
		);
		
	}
	
	private void apiResponseError(DataOutputStream output, String message, String callback) throws Exception {
		
		this.jsonReply(
			output,
			JSONBuilder.f().
				$('[').
					$('{').
						k("status").v("ERROR").
						k("message").v(message).
					$('}').
				$(']'),
			callback
		);
		
	}
	
	private void jsonReply(DataOutputStream output, JSONBuilder json, String callback) throws Exception {
		
		this.jsonReply(
			output,
			json,
			callback,
			null
		);
		
	}
	
	private void jsonReply(DataOutputStream output, JSONBuilder json, String callback, String cookie) throws Exception {
		
		if(cookie != null) {
			
			String[] headers =
				new String[HttpResponse.JSON_HEADERS.length + 1];
				
			for(int i=0; i<HttpResponse.JSON_HEADERS.length; i++) {
				headers[i] = HttpResponse.JSON_HEADERS[i];
			}
			
			headers[headers.length-1] = "Set-Cookie: " + cookie;
				
			HttpResponse.header(
				output,
				headers
			);
			
		} else {
			
			HttpResponse.header(
				output,
				HttpResponse.JSON_HEADERS
			);
			
		}
		
		if(callback != null) {
			output.writeBytes(callback + "(");
		}
		
		output.writeBytes(
			JSONBuilder.f().
				$('{').
					k("data").v(json).
				$('}').getJSON()
		);

		if(callback != null) {
			output.writeBytes(");");
		}
		
		output.flush();
		
	}
	
	private void jsonReply(DataOutputStream output, String json, String callback) throws Exception {
		
		HttpResponse.header(
			output,
			HttpResponse.JSON_HEADERS
		);
		
		if(callback != null) {
			output.writeBytes(callback + "(");
		}
		
		output.writeBytes(
			JSONBuilderDynamic.f().
				$('{').
					k("data").v(json, false).
				$('}').getJSON()
		);

		if(callback != null) {
			output.writeBytes(");");
		}
		
		output.flush();
		
	}
	
	private void textReply(DataOutputStream output, String text) throws Exception {
		
		HttpResponse.header(
			output,
			HttpResponse.CSV_HEADERS
		);
		
		output.writeBytes(text);
		
		output.flush();
		
	}
	
	public JSONBuilder apiEndpointsJSONBuilder() {
		
		return this.apiEndpointsJSONBuilder;
		
	}
	
	public FuseBaseRESTAPIMethod[] httpEndpointMethods() {
		
		return this.httpEndpointMethods.values().toArray(
			new FuseBaseRESTAPIMethod[0]
		);
		
	}
	
}