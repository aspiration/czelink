define(function() {
	return function($scope, secureDataRetriever, require, orchestration) {

		var loginModal = undefined;
		var loginPanel = undefined;
		var registerPanel = undefined;

		$scope.loginPanelActivated = true;
		$scope.registerPanelActivated = false;

		$scope.loginfailedMessage = false;

		$scope.registerLoginModal = function(element, context) {
			loginModal = element[0];
		};

		$scope.registerLoginPanel = function(element, context) {
			loginPanel = element[0];
		};

		$scope.showLoginPanel = function() {
			$(loginPanel).tab("show");
			$scope.registerPanelActivated = false;
			$scope.loginPanelActivated = true;
		};

		$scope.isLoginPanelActivated = function() {
			return $scope.loginPanelActivated;
		};

		$scope.registerRegisterPanel = function(element, context) {
			registerPanel = element[0];
		};

		$scope.showRegisterPanel = function() {
			$(registerPanel).tab("show");
			$scope.registerPanelActivated = true;
			$scope.loginPanelActivated = false;
		};

		$scope.isRegisterPanelActivated = function() {
			return $scope.registerPanelActivated;
		};

		$scope.isActivated = false;
		$scope.isActivatedFail = false;

		$scope.checkIsActivatedMode = function() {
			return $scope.isActivated;
		};

		$scope.checkIsActivatedFail = function() {
			return $scope.isActivatedFail;
		};

		orchestration.expose("openLoginModal", function() {
			$(loginModal).modal('show');
		});

		$scope.disableDuringSubmit = false;

		$scope.initLoginInfoPanelInvalid = false;

		$scope.checkIfLoginInfoInvalid = function() {
			return $scope.initLoginInfoPanelInvalid
					&& ($scope.loginForm.username.$error.required
							|| $scope.loginForm.password.$error.maxlength
							|| $scope.loginForm.password.$error.minlength
							|| $scope.loginForm.username.$error.email
							|| $scope.loginForm.password.$error.required || $scope.loginfailedMessage);
		};

		$scope.checkLoginButtonStatus = function() {
			return ($scope.isRegisterPanelActivated() || $scope.disableDuringSubmit);
		};

		$scope.isUnderProcessing = function() {
			return $scope.disableDuringSubmit;
		};

		var resetLoginModel = function() {
			$scope.initLoginInfoPanelInvalid = false;
			$scope.username = "";
			$scope.password = "";
		};

		$scope.login = function() {

			$scope.isActivatedFail = false;
			$scope.disableDuringSubmit = true;

			$scope.initLoginInfoPanelInvalid = true;
			$scope.loginfailedMessage = false;

			if (!$scope.checkIfLoginInfoInvalid()) {
				secureDataRetriever.setData({
					"username" : $scope.username,
					"password" : $scope.password
				});

				secureDataRetriever.onSuccess(function(data) {
					if (data.status) {
						$(loginModal).modal('hide');
						resetLoginModel();

						$scope.loginfailedMessage = false;

						orchestration.invoke('navigation', 'refreshStatus');
					} else {
						$scope.loginfailedMessage = true;
					}

					$scope.disableDuringSubmit = false;

					if (!$scope.$$phase) {
						$scope.$apply();
					}
					return false;
				});

				secureDataRetriever.onFailure(function() {
					$scope.loginfailedMessage = true;

					$scope.disableDuringSubmit = false;

					if (!$scope.$$phase) {
						$scope.$apply();
					}
					return false;
				});

				secureDataRetriever.post("usermgmt/login");
			} else {
				$scope.disableDuringSubmit = false;
			}
		};

		$scope.checkLoginStatus = function() {
			return $scope.loginfailedMessage;
		};

		$scope.checkPreRegisterStatusValid = function() {
			return ($scope.newpassword === $scope.confirmpassword);
		};

		$scope.checkPostRegisterStatusInvalid = function() {
			return ($scope.registerResult === 0);
		};

		$scope.registerInvalidInit = false;

		$scope.registerResult = -1;
		$scope.registerFailReason = "";
		$scope.registerFailRsnCde = "";

		$scope.checkIfRegisterInfoInvalid = function() {
			return $scope.registerInvalidInit
					&& ($scope.registerForm.newusername.$error.required
							|| $scope.registerForm.newusername.$error.email
							|| $scope.registerForm.newdisplayname.$error.required
							|| $scope.registerForm.newdisplayname.$error.maxlength
							|| $scope.registerForm.newpassword.$error.required
							|| $scope.registerForm.newpassword.$error.maxlength
							|| $scope.registerForm.newpassword.$error.minlength
							|| $scope.registerForm.confirmpassword.$error.maxlength
							|| $scope.registerForm.confirmpassword.$error.minlength
							|| !$scope.checkPreRegisterStatusValid() || ($scope.registerResult === 0));
		};

		$scope.checkRegisterStatus = function() {
			if ($scope.registerResult === 1) {
				return true;
			} else {
				return false;
			}
		};

		$scope.registerValidateErrors = [];

		$scope.register = function() {

			$scope.disableDuringSubmit = true;

			$scope.registerInvalidInit = true;
			$scope.registerResult = -1;

			if (!$scope.checkIfRegisterInfoInvalid()) {
				$scope.registerInvalidInit = true;

				var activatelinkRoot = "";
				if (window.location.origin !== undefined
						&& window.location.origin != null
						&& window.location.origin !== "") {
					activatelinkRoot = window.location.origin
							+ window.location.pathname + "usermgmt/activate";
				} else {
					activatelinkRoot = window.location.protocol + "//"
							+ window.location.host + window.location.pathname
							+ "usermgmt/activate";
				}

				secureDataRetriever.setData({
					newusername : $scope.newusername,
					newpassword : $scope.newpassword,
					newdisplayname : $scope.newdisplayname,
					activatelinkRoot : activatelinkRoot
				});

				secureDataRetriever.onFailure(function() {
					$scope.disableDuringSubmit = false;
					return false;
				});

				secureDataRetriever
						.onSuccess(function(data) {

							if (data.status) {
								$scope.registerResult = 1;
							} else {
								if (data.validateErrors !== undefined
										&& data.validateErrors !== null
										&& data.validateErrors.length > 0) {
									$scope.registerValidateErrors = data.validateErrors;
									$scope.registerResult = 0;
									$scope.registerFailReason = "非法输入无法通过校验";
								} else {
									$scope.registerResult = 0;
									if (data.statusCode === "002") {
										$scope.registerFailReason = "该邮箱已经注册";
										$scope.registerFailRsnCde = "002";
									}
									if (data.statusCode == "008"
											|| data.statusCode == "010"
											|| data.statusCode == "012") {
										$scope.registerFailReason = "服务器异常，请联系管理员";
										$scope.registerFailRsnCde = "008";
									}
									$scope.registerValidateErrors = [];
								}
							}

							$scope.disableDuringSubmit = false;

							if (!$scope.$$phase) {
								$scope.$apply();
							}
							return false;
						});

				secureDataRetriever.post("usermgmt/register");
			} else {
				$scope.disableDuringSubmit = false;
			}
		};

		$scope.checkRegisterButtonStatus = function() {
			return (($scope.isLoginPanelActivated())
					|| ($scope.registerResult == 1) || $scope.disableDuringSubmit);
		};

		return function() {

			var adjustWindowLocation = function() {
				var currentLocation = window.location.href;
				if (currentLocation.indexOf("#") === -1) {
					var newLocation = currentLocation.replace(currentLocation
							.substring(currentLocation.indexOf("?"),
									currentLocation.length), "");
					window.history.pushState(null, null, newLocation);
				} else {
					var newLocation = currentLocation.replace(currentLocation
							.substring(currentLocation.indexOf("?"),
									currentLocation.indexOf("#")), "");
					window.history.pushState(null, null, newLocation);
				}
			};

			var failprocess = function() {
				$scope.isActivatedFail = true;

				adjustWindowLocation();

				$scope.intervalTime = 5;
				var intervalMethod = setInterval(function() {
					$scope.intervalTime--;
					if ($scope.intervalTime <= 0) {
						clearInterval(intervalMethod);
						$scope.isActivatedFail = false;
					}
					if (!$scope.$$phase) {
						$scope.$apply();
					}
				}, 1000);

				if (!$scope.$$phase) {
					$scope.$apply();
				}
			};

			var checkStatus = function(activateInstance, verifyKey) {
				secureDataRetriever.setData({
					activateInstance : activateInstance,
					verifyKey : verifyKey
				});

				secureDataRetriever.onSuccess(function(data) {
					if (data.status) {
						$scope.isActivated = true;
						$scope.username = activateInstance;

						if (!$scope.$$phase) {
							$scope.$apply();
						}

						adjustWindowLocation();

						$(loginModal).modal('show');
					} else {
						failprocess();
					}
				});

				secureDataRetriever.post("usermgmt/checkActivateStatus");
			};

			orchestration
					.invoke(
							"navigation",
							"getActivatedInstance",
							null,
							function(activateInstance) {
								orchestration
										.invoke(
												"navigation",
												"getVerifyKey",
												null,
												function(verifyKey) {
													if (activateInstance !== undefined
															&& activateInstance !== null
															&& verifyKey !== undefined
															&& verifyKey !== null) {
														checkStatus(
																activateInstance,
																verifyKey);
													} else if (activateInstance !== undefined
															&& activateInstance !== null
															&& (verifyKey === undefined || verifyKey === null)) {
														failprocess();
													} else if ((activateInstance === undefined || activateInstance === null)
															&& verifyKey !== undefined
															&& verifyKey !== null) {
														failprocess();
													} else {
														// do nothing - normal
														// case.
													}
												});
							});
		};
	};
});