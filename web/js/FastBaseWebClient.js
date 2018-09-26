function success(message) {
	$('#feedbackMessage').html(message);
	var feedback =
		$('#feedback');
	feedback.addClass('alert-success');
	feedback.removeClass('alert-info');
	feedback.removeClass('alert-danger');
	feedback.show();
}

function info(message) {
	$('#feedbackMessage').html(message);
	var feedback =
		$('#feedback');
	feedback.addClass('alert-info');
	feedback.removeClass('alert-success');
	feedback.removeClass('alert-error');
	feedback.show();
}

function error(message) {
	$('#feedbackMessage').html(message);
	var feedback =
		$('#feedback');
	feedback.addClass('alert-danger');
	feedback.removeClass('alert-success');
	feedback.show();
}

function processSourceCodeInput(e, element) {
	var keyCode = e.keyCode || e.which;

	// Tab support
	if(keyCode == 9) {
		e.preventDefault();

		var start = element.selectionStart;
		var end = element.selectionEnd;

		// set textarea value to: text before caret + tab + text after caret
		element.value =
			element.value.substring(0, start) + "\t" + element.value.substring(end);

		// put caret at right position again
		element.selectionStart =
			element.selectionEnd = start + 1;
	}

}

function FastBaseClient($http) {
	this.init =
		function() {
			this.queries.init(this);
			this.connections.init(this);
			this.users.init(this);
			this.roles.init(this);
			this.scripts.init(this);
			this.scheduler.init(this);
			this.system.init(this);
		},
	this.listAll =
		function() {
			this.queries.list();
			this.connections.list();
			this.users.list();
			this.roles.list();
			this.scripts.list();
			this.scheduler.list();
		},
	this.buildQueryString =
		function(object, exceptKeys) {
			var queryString = "";
			var i = 0;
			for(var key in object) {
				if(exceptKeys && key in exceptKeys) {
					continue;
				}
				if(i++ > 0) {
					queryString += "&";
				}
				queryString +=
					key + "=" + encodeURIComponent(object[key]);
			}
			return queryString;
		},
	this.currentURL =
		function() {
			
			var location =
				window.location.href;
				
			if(location.indexOf('://') > -1) {
				
				location =
					location.substring(
						0,
						location.indexOf(
							'/',
							location.indexOf('://') + 3
						)
					);
				
			} else {
				
				location =
					location.substring(
						0,
						location.indexOf(
							'/'
						)
					);
				
			}
			
			return location;
			
		},
	this.roles = {
		
		parent: null,
		
		data: [],
		current: {},
		new: {},
		
		roleTypes: [],
		
		init:
			function(parent) {
				this.parent = parent;
				this.list();
				this.getRoleTypes();
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/users/roles").success(
					function(response) {
				
						me.data = response.data;
						
					}
				);
				
			},
		gatherPrivileges:
			function(role) {
				
				var privilegeList = [];
				
				for(var privilege in role.addedPrivileges) {
					
					privilegeList.push(privilege);
					
				}
				
				return {
					
					'privilegeList': privilegeList
					
				};
				
			},
		gatherPrivilegesDelta:
			function(role) {

				var privilegeList = [],
					privilegeListToRemove = [];

				for(var privilege in role.addedPrivileges) {

					if(role.addedPrivileges[privilege] && !role.existingPrivileges[privilege]) {
						
						privilegeList.push(privilege);
						
					} else if(role.existingPrivileges[privilege] && !role.addedPrivileges[privilege]) {
						
						privilegeListToRemove.push(privilege);
						
					}

				}

				return {

					'privilegeList': privilegeList,
					'privilegeListToRemove': privilegeListToRemove

				};

			},
		saveRole:
			function(role) {
				
				var me = this;
				
				var privileges =
					this.gatherPrivileges(role);
					
				var queryString =
					"name=" + role.name +
					"&roleType=" + encodeURIComponent(role.roleType) +
					"&privilegeList=" + encodeURIComponent(privileges.privilegeList.join(","));

				$http.get("/users/roles/add?" + queryString).success(
					function(response) {

						var feedback =
							response.data[0];

						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}

						me.list();

					}
				);

				$('#newRoleModal').modal('hide');
				
			},
		editPrivileges:
			function(role) {
				
				this.current =
					role;
				
				this.current.existingPrivileges = {};
				this.current.addedPrivileges = {};
				
				for(var idx in this.current.privileges) {
					
					this.current.existingPrivileges[this.current.privileges[idx]] = true;
					this.current.addedPrivileges[this.current.privileges[idx]] = true;
					
				}
				
			},
		updateRolePrivileges:
			function(role) {
				
				var privilegesDelta =
					this.gatherPrivilegesDelta(role);
					
				if(privilegesDelta.privilegeList.length == 0 && privilegesDelta.privilegeListToRemove.length == 0) {
					
					$('#updateRolePrivilegesModal').modal('hide');
					
					return;
					
				}
					
				var me = this;
				
				var queryString =
					"name=" + role.name +
					"&privilegeList=" + encodeURIComponent(privilegesDelta.privilegeList) +
					"&privilegeListToRemove=" + encodeURIComponent(privilegesDelta.privilegeListToRemove)
				
				$http.get("/users/roles/update/privileges?" + queryString).success(
					function(response) {

						var feedback =
							response.data[0];

						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}

						me.list();

					}
				);
				
				$('#updateRolePrivilegesModal').modal('hide');
				
			},
		delete:
			function(role) {
				
				if(!confirm('Would you like to delete role "' + role.name + '"?')) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/users/roles/delete?name=" + role.name).success(
					function(response) {

						var feedback =
							response.data[0];

						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}

						me.list();

					}
				);
				
			},
		validRole:
			function(role) {
				
				return (role.name && role.name.length > 0 && role.roleType && role.roleType.length > 0);
				
			},
		newRole:
			function() {
				this.new = {};
			},
		getRoleTypes:
			function() {

				this.roleTypes = [];
				var me = this;

				$http.get("/users/roles/types").success(
					function(response) {

						me.roleTypes =
							response.data;

					}
				);

			}
	},
	this.users = {
		
		parent: null,
		
		data: [],
		current: {},
		new: {},
		
		privileges: [],
		
		init:
			function(parent) {
				this.parent = parent;
				this.getPrivileges();
				this.list();
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/users").success(
					function(response) {
				
						me.data = response.data;
						
					}
				);
				
			},
		setCurrent:
			function(user) {
				
				this.current = user;
				
			},
		passwordsMatchForNewUser:
			function() {
				return (
					this.new.password &&
					this.new.password_repeated &&
					this.new.password.length > 0 &&
					this.new.password_repeated.length > 0 &&
					this.new.password == this.new.password_repeated
				);
			},
		passwordsMatch:
			function() {
				return (
					this.current.old_password &&
					this.current.old_password_repeated &&
					this.current.old_password.length > 0 &&
					this.current.old_password_repeated.length > 0 &&
					this.current.old_password == this.current.old_password_repeated
				);
			},
		newUser:
			function() {
				this.new = {};
			},
		editRolesAndPrivileges:
			function(user) {
				
				this.current =
					user;
					
				this.current.existingRoles = {};
				this.current.addedRoles = {};
				this.current.existingPrivileges = {};
				this.current.addedPrivileges = {};
				
				for(var idx in this.current.roles) {
					
					this.current.existingRoles[this.current.roles[idx]] = true;
					this.current.addedRoles[this.current.roles[idx]] = true;
					
				}
				
				for(var idx in this.current.privileges) {
					
					this.current.existingPrivileges[this.current.privileges[idx]] = true;
					this.current.addedPrivileges[this.current.privileges[idx]] = true;
					
				}
				
			},
		editPassword:
			function(user) {
				this.current = user;
				delete this.current['old_password'];
				delete this.current['old_password_repeated'];
				delete this.current['new_password'];
			},
		changePasswordForCurrent:
			function() {
				
				if(this.changePasswordInputIsInvalid()) {
					return;
				}
				
				var
					h1 =
						encodeURIComponent(
							window.btoa(
								this.current.username + ':' +
								this.current.old_password
							)
						),
					h2 =
						encodeURIComponent(
							window.btoa(
								this.current.username + ':' +
								this.current.new_password
							)
						);
				
				$http.get("/user/changepassword?h1=" + h1 + "&h2=" + h2).success(
					function(response) {

						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
					}
				);
				
				$('#userPasswordModal').modal('hide');
				
			},
		changePasswordInputIsInvalid:
			function() {
				return !(
					this.passwordsMatch() &&
					this.current.new_password &&
					this.current.new_password.length > 0
				);
			},
		delete:
			function(user) {
				
				if(!confirm('Would you like to delete user "' + user.username + '"?')) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/user/delete?" + me.parent.buildQueryString(user, {'$$hashKey': ''})).success(
					function(response) {

						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);
				
			},
		gatherUserRolesAndPrivileges:
			function(user) {
				
				var roleList = [],
					roleListToRemove = [],
					privilegeList = [],
					privilegeListToRemove = [];
				
				for(var role in user.addedRoles) {
					
					roleList.push(role);
					
				}
				
				for(var privilege in user.addedPrivileges) {
					
					privilegeList.push(privilege);
					
				}
				
				return {
					
					'roleList': roleList,
					'privilegeList': privilegeList
					
				};
				
			},
		gatherUserRolesAndPrivilegesDelta:
			function(user) {
				
				var roleList = [],
					roleListToRemove = [],
					privilegeList = [],
					privilegeListToRemove = [];
				
				for(var role in user.addedRoles) {
					
					if(user.addedRoles[role] && !user.existingRoles[role]) {
						
						roleList.push(role);
						
					} else if(user.existingRoles[role] && !user.addedRoles[role]) {
						
						roleListToRemove.push(role);
						
					}
					
				}
				
				for(var privilege in user.addedPrivileges) {
					
					if(user.addedPrivileges[privilege] && !user.existingPrivileges[privilege]) {
						
						privilegeList.push(privilege);
						
					} else if(user.existingPrivileges[privilege] && !user.addedPrivileges[privilege]) {
						
						privilegeListToRemove.push(privilege);
						
					}
					
				}
				
				return {
					
					'roleList': roleList,
					'roleListToRemove': roleListToRemove,
					'privilegeList': privilegeList,
					'privilegeListToRemove': privilegeListToRemove
					
				};
				
			},
		updateUserPriviligesAndRoles:
			function(user) {
				
				var me = this;
				
				var userRolesAndPrivilegesDelta =
					this.gatherUserRolesAndPrivilegesDelta(user);
				
				var queryString =
					"username=" + encodeURIComponent(user.username) +
					"&privilegeList=" + encodeURIComponent(userRolesAndPrivilegesDelta.privilegeList.join(",")) +
					"&privilegeListToRemove=" + encodeURIComponent(userRolesAndPrivilegesDelta.privilegeListToRemove.join(",")) +
					"&roleList=" + encodeURIComponent(userRolesAndPrivilegesDelta.roleList.join(",")) +
					"&roleListToRemove=" + encodeURIComponent(userRolesAndPrivilegesDelta.roleListToRemove.join(","));
				
				$http.get("/user/privilegesandroles/update?" + queryString).success(
					function(response) {

						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);
				
				$('#userRolesAndPrivilegesModal').modal('hide');
				
			},
		saveNewUser:
			function(user) {
				
				if(this.userIsInvalid(user)) {
					
					return;
					
				}
				
				var me = this;
				
				var h =
					encodeURIComponent(
						window.btoa(
							user.username + ':' +
							user.password
						)
					);
					
				var userRolesAndPrivileges =
					this.gatherUserRolesAndPrivileges(user);
				
				var queryString =
					"h=" + h +
					"&roleList=" + encodeURIComponent(userRolesAndPrivileges.roleList.join(",")) +
					"&privilegeList=" + encodeURIComponent(userRolesAndPrivileges.privilegeList.join(","));
				
				$http.get("/user/add?" + queryString).success(
					function(response) {

						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);
				
				$('#newUserModal').modal('hide');
				
			},
		userIsInvalid:
			function(user) {
				
				return !(
					user.password &&
					user.password.length > 0 &&
					user.password_repeated &&
					user.password_repeated.length > 0 &&
					user.password == user.password_repeated
				);
				
			},
		getClientKeys:
			function(user) {
				
				$http.get("/user/clientkeys?username=" + user.username).success(
					function(response) {

						user.clientKeys =
							response.data;

					}
				);
				
			},
		generateClientKey:
			function(user) {
				
				var me = this;
				
				$http.get("/user/generateclientkey?username=" + user.username).success(
					function(response) {
							
						me.getClientKeys(user);

					}
				);
				
			},
		deleteClientKey:
			function(user, clientKey) {
				
				if(!confirm("Do you wish to delete the client key \"" + clientKey + "\" for user \"" + user.username + "\"?")) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/user/clientkey/delete?username=" + user.username + "&clientkey=" + clientKey).success(
					function(response) {
							
						me.getClientKeys(user);

					}
				);
				
			},
		deleteAllClientKeys:
			function(user) {
				
				if(!confirm("Do you wish to delete all client keys for user \"" + user.username + "\"?")) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/user/clientkeys/deleteall?username=" + user.username).success(
					function(response) {
							
						me.getClientKeys(user);

					}
				);
				
			},
		getPrivileges:
			function() {
				
				this.privileges = [];
				var me = this;

				$http.get("/api/privileges").success(
					function(response) {

						me.privileges =
							response.data;

					}
				);
				
			}
	},
	this.queries = {
		
		parent: null,
		
		data: [],
		current: {},
		new: {},
		queryResults: {},
		
		queryTypes: [],
		
		init:
			function(parent) {
				this.parent = parent;
				this.getQueryTypes();
				this.list();
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/connections/registeredqueries").success(
					function(response) {
				
						me.data = response.data;
						
					}
				);
				
			},
		runQuery:
			function(queryObject) {
				
				this.queryResults = {};
				var me = this;
				
				$http.get("/connections/query?" + me.parent.buildQueryString(queryObject, {'$$hashKey': '', 'queryId': ''})).success(
					function(response) {
				
						me.queryResults =
							response.data;
						
					}
				);
			},
		runRegisteredQuery:
			function(queryObject) {

				this.queryResults = {};
				var me = this;

				$http.get("/connections/registeredquery?" + me.parent.buildQueryString(queryObject, {'$$hashKey': '', 'query': ''})).success(
					function(response) {

						me.queryResults =
							response.data;

					}
				);
				
			},
		registerQuery:
			function(event, queryObject) {

				var me = this;
				
				$(event.target).prop('disabled', true);
				
				$http.get("/connections/query/register?" + me.parent.buildQueryString(queryObject, {'$$hashKey': ''})).success(
					function(response) {

						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.clearNew();
						me.list();
						
						$('#registerQueryModal').modal('hide');
						$(event.target).prop('disabled', false);
						
					}
				).error(
					function() {
						$(event.target).prop('disabled', false);
						$('#registerQueryModal').modal('hide');
					}
				);
			},
		delete:
			function(queryObject) {
				
				if(!confirm("Do you wish to delete Query Id \"" + queryObject.queryId + "\"?")) {
					return;
				}
				
				var me = this;

				$http.get("/connections/query/delete?queryId=" + queryObject.queryId).success(
					function(response) {
						
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();

					}
				);
				
			},
		clearNew:
			function() {
				this.new = {};
			},
		clearCurrent:
			function() {
				this.current = {};
				this.queryResults = {};
			},
		getQueryTypes:
		 	function() {
				this.queryTypes = [];
				var me = this;

				$http.get("/connections/querytypes").success(
					function(response) {

						me.queryTypes =
							response.data;

					}
				);
			}
	},
	this.scripts = {
		
		parent: null,
		
		data: [],
		scriptNamesToDataIndex: {},
		current: {},
		scriptOutput: '',
		
		init:
			function(parent) {
				this.parent = parent;
				this.list();
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/scripts").success(
					function(response) {
				
						me.data = response.data;
						
						for(var scriptIdx=0; scriptIdx<me.data.length; scriptIdx++) {
							
							me.scriptNamesToDataIndex[me.data[scriptIdx].name] = scriptIdx;
							
						}
						
						/*var source;
						var modifiedSource;
						
						var c;
						var inSingleQuote = false, inDoubleQuote = false;
						
						for(var scriptIdx=0; scriptIdx<me.data.length; scriptIdx++) {
							
							source = me.data[scriptIdx].source;
							modifiedSource = '';
							
							c = '';
							inSingleQuote = false;
							inDoubleQuote = false;
							
							for(var i=0; i<source.length; i++) {

								c = source.charAt(i);

								if(c == '\'' && !inDoubleQuote) {
									inSingleQuote = !inSingleQuote;
								}
								
								if(c == '"' && !inSingleQuote) {
									inDoubleQuote = !inDoubleQuote;
								}

								if((inSingleQuote || inDoubleQuote) && c == '\n') {
									modifiedSource += '\\n';
								} else {
									modifiedSource += c;
								}

							}
							
							me.data[scriptIdx].source =
								modifiedSource;
							
						}*/
						
					}
				);
				
			},
		getSource:
			function(script) {
				
				var me = this;
				
				$http.get("/scripts/source?name=" + script.name).success(
					function(response) {
				
						me.data[me.scriptNamesToDataIndex[script.name]]["source"] = response;
						
					}
				);
				
			},
		new:
			function() {
				
				this.current = {};
				
			},
		save:
			function(script) {
				
				var me = this;
				
				/*$http.get("/scripts/upload?name=" + script.name + "&source=" + encodeURIComponent(script.source)).success(
					function(response) {
				
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
						// Hacky :-(
						$('#registerScriptModal').modal('hide');
						$('#editScriptModal').modal('hide');
						
					}
				);*/
				
				$http({
					method: 'post',
					url: '/scripts/upload?name=' + script.name,
					data: script.source
				}).success(
					function(response) {

						var feedback =
							response.data[0];
						
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
					
						me.list();
					
						// Hacky :-(
						$('#registerScriptModal').modal('hide');
						$('#editScriptModal').modal('hide');

					}
				);
				
			},
		delete:
			function(script) {
				
				if(!confirm('Do you wish to delete script "' + script.name + '"?')) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/scripts/delete?name=" + script.name).success(
					function(response) {
				
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);
				
			},
		runAnonymous:
			function(script) {
				
				this.scriptOutput = '';
				
				var me = this;
				
				/*$http.get("/scripts/run/anonymous?source=" + encodeURIComponent(script.source)).success(
					function(response) {
				
						me.scriptOutput =
							JSON.stringify(response);
						
					}
				);*/
					
				$http({
					method: 'post',
					url: '/scripts/run/anonymous',
					data: script.source
				}).success(
					function(response) {

						me.scriptOutput =
							JSON.stringify(response);

					}
				);
				
			},
		run:
			function(script) {
				
				this.scriptOutput = '';
				
				var me = this;
				
				$http.get("/scripts/run?name=" + script.name).success(
					function(response) {
				
						me.scriptOutput =
							JSON.stringify(response);
						
					}
				);
				
			}
		
	},
	this.scheduler = {
		
		parent: null,
		
		data: [],
		timeUnits: [],
		logFileContents: '',
		
		init:
			function(parent) {
				this.parent = parent;
				this.list();
				this.getTimeUnits();
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/scheduler/jobs").success(
					function(response) {
				
						me.data = response.data;
						
					}
				);
				
			},
		isJobInvalid:
			function(job) {
				
				if(job == undefined) {
					
					return false;
					
				}
				
				return (
					job.name == undefined ||
					job.name.length == 0 ||
					job.scriptName == undefined ||
					job.scriptName.length == 0 ||
					job.timeUnit == undefined ||
					job.timeUnit.length == 0 ||
					!Number.isInteger(parseInt(job.initialDelay)) ||
					!Number.isInteger(parseInt(job.period))
				);
			},
		saveJob:
			function(job) {
				
				var me = this;
				
				var queryString =
					'name=' + job.name +
					'&scriptName=' + job.scriptName +
					'&timeUnit=' + job.timeUnit +
					'&initialDelay=' + job.initialDelay +
					'&period=' + job.period;
				
				$http.get("/scheduler/jobs/create?" + queryString).success(
					function(response) {
				
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
						$('#registerScheduledJobModal').modal('hide');
						
					}
				);
				
			},
		delete:
			function(job) {
				
				if(!confirm('Do you wish to delete scheduled job "' + job.name + '"?')) {
					
					return;
					
				}
				
				var me = this;
				
				$http.get("/scheduler/jobs/delete?name=" + job.name).success(
					function(response) {
				
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);
				
			},
		start:
			function(job) {
				
				var me = this;
				
				$http.get("/scheduler/jobs/start?name=" + job.name).success(
					function(response) {
				
						me.list();
						
					}
				);
				
			},
		viewLogs:
			function(job) {
				
				this.currentJob = job;
				this.logFileContents = '';
				
				$http.get("/scheduler/jobs/logs?name=" + job.name).success(
					function(response) {
				
						job['logEntries'] =
							response.data;
						
					}
				);
				
			},
		getLogFile(job, logEntry) {
			
			var me = this;
			
			$http.get("/scheduler/jobs/logs/getfile?name=" + job.name + "&fileName=" + logEntry.fileName).success(
				function(response) {
			
					me.logFileContents = response;
					
				}
			);
			
		},
		getTimeUnits:
			function() {
				
				var me = this;
				
				$http.get("/scheduler/timeunits").success(
					function(response) {
				
						me.timeUnits = response.data;
						
					}
				);
				
			}
		
	},
	this.system = {
		
		parent: null,
		importFile: null,
		
		init:
			function(parent) {
				this.parent = parent;
			},
		import:
			function() {
				
				var file =
					document.getElementById('importFile').files[0];
					
				if(file == undefined) {
					
					alert('Please select a file.');
					return;
					
				}
				
				var fileReader =
					new FileReader();
					
				var me = this;
					
				// Read file data
				fileReader.onload =
					function(e) {
						
						$http({
							method: 'post',
							url: '/system/import/all',
							data: new Uint8Array(fileReader.result),
							transformRequest: []
						}).success(
							function(response) {

								var feedback =
									response.data[0];
								
								if(feedback.status == 'OK') {
									success(feedback.message);
								} else if(feedback.status == 'ERROR') {
									error(feedback.message);
								}
								
								me.parent.listAll();

							}
						);
					}
					
				fileReader.readAsArrayBuffer(file);
				
			}
	},
	this.connections = {
		
		parent: null,
		
		data: [],
		dataDefault: 'Please select connection...',
		old: {},
		current: {},
		
		jdbcDrivers: [],
		jdbcDriversLookup: {},
		jdbcDriversDefaultChoice: 'Please select driver...',
		
		init:
			function(parent) {
				this.parent = parent;
				this.list();
				this.getJDBCDrivers();
			},
		connect:
			function(event, connectionName) {
				
				var me = this;
				
				$(event.target).prop('disabled', true);
				
				$http.get("/connections/connect?connectionName=" + connectionName).success(
					function(response) {
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
						$(event.target).prop('disabled', false);
					}
				).error(
					function() {
						$(event.target).prop('disabled', false);
					}
				);

			},
		new:
			function() {
				this.current = {
					'isNew': true
				};
			},
		edit:
			function(connectionObject) {
				
				this.current = connectionObject;
				this.current['passWord'] = '';
				this.current['isNew'] = false;
				
				this.old =
					angular.copy(this.current);
				
			},
		list:
			function() {
				
				var me = this;
				
				$http.get("/connections/list").success(
					function(response) {
				
						me.data = response.data;
						
					}
				);
				
			},
		delete:
			function(connectionName) {
				
				if(!confirm('Do you wish to delete connection "' + connectionName + '"?')) {
					return;
				}
				
				var me = this;
				
				$http.get("/connections/delete?connectionName=" + connectionName).success(
					function(response) {
						var feedback =
							response.data[0];
							
						if(feedback.status == 'OK') {
							success(feedback.message);
						} else if(feedback.status == 'ERROR') {
							error(feedback.message);
						}
						
						me.list();
						
					}
				);

			},
		saveCurrent:
			function() {
				
				if(this.current.jdbcDriverInfoName == this.jdbcDriversDefaultChoice) {
					alert('Please select JDBC Driver.');
					return;
				}
				
				if(this.current.connectionName.length == 0) {
					alert('Please provide a connection name.');
					return;
				}
				
				if(this.current.connectString.length == 0) {
					alert('Please provide a connection string.');
					return;
				}
				
				if(this.current.user.length == 0) {
					alert('Please provide a user.');
					return;
				}
				
				var save = false;
				
				if(this.current.isNew) {
					
					save = true;
					
				} else if(!this.current.isNew) {
					
					if(this.inputHasChanged()) {
						
						save = true;
						
					} else {
						
						info("Nothing has changed. No action performed.");
						
						save = false;
						
					}
					
				}
				
				if(save) {
					
					var me = this;
					var queryString = "";
					var i = 0;
					
					$http.get("/connections/addorupdate?" + me.parent.buildQueryString(this.current, {'$$hashKey': ''})).success(
						function(response) {
							var feedback =
								response.data[0];

							if(feedback.status == 'OK') {
								success(feedback.message);
							} else if(feedback.status == 'ERROR') {
								error(feedback.message);
							}

							me.list();

						}
					);
					
				}
				
				$('#connectionModal').modal('hide');
				
			},
		setDerivedFields:
			function() {
				
				var jdbcDriverInfo =
					this.jdbcDriversLookup[this.current.jdbcDriverInfoName];
				
				this.current.connectString =
					jdbcDriverInfo.connectStringPattern;
				
				this.current.jdbcDriverClass =
					jdbcDriverInfo.className;
				
			},
		inputHasChanged:
			function() {
				for(key in this.current) {
					if(this.current[key] != this.old[key]) {
						return true;
					}
				}
				return false;
			},
		getJDBCDrivers:
			function() {
				
				var me = this;
				
				$http.get("/connections/jdbcdriverinfo").success(
					function(response) {
						
						me.jdbcDrivers = response.data;
						
						for(var idx in me.jdbcDrivers) {
							
							if(me.jdbcDrivers[idx].name) {
								
								me.jdbcDriversLookup[me.jdbcDrivers[idx].name] =
									me.jdbcDrivers[idx];
								
							} else {
								
								me.jdbcDrivers = [];
								me.jdbcDriversLookup = {};
								
								break;
								
							}
						}
						
					}
				);
				
			}
	};
}

$(document).ready(
	function () {
		
		// Set active nav link according to url location
		var url = new String(window.location);
		var link = '#' + url.substring(url.indexOf('#') + 2);
		link = (link == '#' ? link + '/' : link);
		$('ul.nav a[href="'+ link +'"]').parent().addClass('active');
		
		// Remove nav active class for all nav li a except the one clicked
		$('.nav li:not(.notactive) a').click(
			function(e) {
				$('.nav li').removeClass('active');
				var $parent = $(this).parent();
				if (!$parent.hasClass('active')) {
					$parent.addClass('active');
				}
			}
		);
		
	}
);

var fastBaseApp =
	angular.module(
		'ngFastBase', 
		['ngRoute', 'ui.bootstrap']
	);

// Configure routes
fastBaseApp.config(
	function($routeProvider) {
		$routeProvider
			// route for the home page
			.when('/', {
				templateUrl : 'pages/home.html',
				controller  : 'homeController'
			})
		
			// route for the connections page
			.when('/connections', {
				templateUrl : 'pages/connections.html',
				controller  : 'connectionsController'
			})
		
			// route for the queries page
			.when('/queries', {
				templateUrl : 'pages/queries.html',
				controller  : 'queriesController'
			})
			
			// route for the scripts page
			.when('/scripts', {
				templateUrl : 'pages/scripts.html',
				controller  : 'scriptsController'
			})
			
			// route for the users page
			.when('/users', {
				templateUrl : 'pages/users.html',
				controller  : 'usersController'
			})
			
			// route for the system page
			.when('/system', {
				templateUrl : 'pages/system.html',
				controller  : 'systemController'
			})
	}
);

var fbClient;

// Common controller for top-level page
fastBaseApp.controller(
	'FastBaseWebClientControl',
	function($scope, $http) {
		
		$http.get("/user/current").success(
			function(response) {
				
				$scope.currentUsername =
					response.data[0].username;
			}
		);
		
		$scope.fastBaseClient =
			new FastBaseClient($http);
			
		fbClient = $scope.fastBaseClient;
			
		$scope.fastBaseClient.init();
		
		$scope.angular = angular;
		
	}
);

// Controllers for sub-pages
fastBaseApp.controller(
	'homeController',
	function($scope) {
		;
	}
);

fastBaseApp.controller(
	'connectionsController',
	function($scope, $http) {
		;
	}
);

fastBaseApp.controller(
	'queriesController',
	function($scope) {
		;
	}
);

fastBaseApp.controller(
	'scriptsController',
	function($scope) {
		;
	}
);

fastBaseApp.controller(
	'usersController',
	function($scope) {
		;
	}
);

fastBaseApp.controller(
	'systemController',
	function($scope) {
		;
	}
);