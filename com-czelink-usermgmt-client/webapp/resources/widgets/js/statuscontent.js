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

		$scope.initLoginInfoPanelInvalid = false;

		$scope.checkIfLoginInfoInvalid = function() {
			return $scope.initLoginInfoPanelInvalid
					&& ($scope.loginForm.username.$error.required
							|| $scope.loginForm.username.$error.email
							|| $scope.loginForm.password.$error.required || $scope.loginfailedMessage);
		};

		var resetLoginModel = function() {
			$scope.initLoginInfoPanelInvalid = false;
			$scope.username = "";
			$scope.password = "";
		};

		$scope.login = function() {
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

					if (!$scope.$$phase) {
						$scope.$apply();
					}
					return false;
				});

				secureDataRetriever.onFailure(function() {
					$scope.loginfailedMessage = true;

					if (!$scope.$$phase) {
						$scope.$apply();
					}
					return false;
				});

				secureDataRetriever.post("usermgmt/login");
			}
		};

		$scope.checkLoginStatus = function() {
			return $scope.loginfailedMessage;
		};

		$scope.register = function() {
			// TODO: to finish.
		};
	};
});