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

		var resetLoginModel = function() {
			$scope.initLoginInfoPanelInvalid = false;
			$scope.username = "";
			$scope.password = "";
		};

		$scope.login = function() {

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

		$scope.checkIfRegisterInfoInvalid = function() {
			return $scope.registerInvalidInit
					&& ($scope.registerForm.newusername.$error.required
							|| $scope.registerForm.newusername.$error.email
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

		$scope.register = function() {

			$scope.disableDuringSubmit = true;

			$scope.registerInvalidInit = true;
			$scope.registerResult = -1;

			if (!$scope.checkIfRegisterInfoInvalid()) {
				$scope.registerInvalidInit = true;

				secureDataRetriever.setData({
					username : $scope.newusername,
					password : $scope.newpassword
				});

				secureDataRetriever.onFailure(function() {
					$scope.disableDuringSubmit = false;
					return false;
				});

				secureDataRetriever.onSuccess(function(data) {

					if (data.status) {
						$scope.registerResult = 1;
					} else {
						$scope.registerResult = 0;
						if (data.statusCode === "002") {
							$scope.registerFailReason = "该邮箱已经注册";
						}
						if (data.statusCode == "008") {
							$scope.registerFailReason = "服务器异常，请联系管理员";
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
	};
});