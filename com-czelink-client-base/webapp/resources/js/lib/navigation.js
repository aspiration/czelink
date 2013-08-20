define([ 'jquery', 'require', 'orchestration' ],
		function(jquery, require, orchestration) {

			// prepare loadpath:
			var location = window.location.hash;
			var candidates = [ '#home', '#information', '#weibo', '#blog',
					'#group', '#recruitment', '#community', '#photo',
					'#business_school', '#help' ];
			var loadpath = "text!views/home.html";
			if (candidates.indexOf(location) > -1) {
				var target = location.slice(1, location.length);
				loadpath = "text!views/" + target + ".html";
			} else {
				window.location.hash = "#home";
			}

			var initWidgetApplication = function(widgetName, widgetElement,
					orchestration) {
				var widgetModule = angular.module(widgetName, []);

				var widgetControllerPath = 'widgets/js/' + widgetName;
				require([ widgetControllerPath ], function(widgetController) {
					// define controller
					widgetModule.controller(widgetName + "Ctrl", function(
							$scope) {
						// register to orchestration.
						var orchestrationManager = orchestration(widgetName,
								$scope);

						orchestrationManager.replaceWith = function(
								targetWidgetName) {
							var widget = document
									.querySelectorAll('div[widget='
											+ targetWidgetName + ']');

							// get widget template
							var widgetName = widget.getAttribute('widget');
							// attach controller
							widget.setAttribute('ng-controller', widgetName
									+ 'Ctrl');
							var widgetPath = 'text!widgets/views/' + widgetName
									+ ".html!strip";
							require([ widgetPath ], function(widgetTemplate) {
								widget.innerHTML = widgetTemplate;
								initWidgetApplication(widgetName, widget,
										orchestration);
							});
						};

						// handled by consumer.
						widgetController($scope, jquery, require,
								orchestrationManager);
					});
					angular.bootstrap(widgetElement, [ widgetName ]);
				});
			};

			var handleWidgets = function(orchestration) {
				var widgets = document.querySelectorAll('div[widget]');

				angular.forEach(widgets, function(widget) {
					// get widget template
					var widgetName = widget.getAttribute('widget');
					// attach controller
					widget.setAttribute('ng-controller', widgetName + 'Ctrl');
					var widgetPath = 'text!widgets/views/' + widgetName
							+ ".html!strip";
					require([ widgetPath ],
							function(widgetTemplate) {
								widget.innerHTML = widgetTemplate;
								initWidgetApplication(widgetName, widget,
										orchestration);
							});
				});

			};

			// initialize navigation
			var ngNavbar = angular.module('navigation', []);

			ngNavbar.controller('navCtrl', function($scope) {

				var orchestrationManager = orchestration('navigation', $scope);

				if (window.location.hash === '') {
					$scope.currentHash = '#home';
				} else {
					$scope.currentHash = window.location.hash;
				}

				$scope.activityCheck = function(target) {

					if ($scope.currentHash === target) {
						return true;
					} else {
						return false;
					}
				};

				var flashParams = {};
				var siteParams = {};

				// options: location, flashObjs, siteObjs
				$scope.navigate = function(options) {
					flashParams = {};

					var location = options.location;
					var flashObjs = options.flashObjs;
					var siteObjs = options.siteObjs;

					$scope.currentHash = "#"
							+ location.slice(0, location.indexOf(".html"));

					if (!$scope.$$phase) {
						$scope.$apply();
					}

					if (flashObjs !== null || flashObjs !== undefined) {
						flashParams = flashObjs;
					}

					if (siteObjs !== null || siteObjs !== undefined) {
						angular.extend({}, siteParams, siteObjs);
					}

					var main_content = document.getElementById('main_content');
					var loadpath = "text!views/" + location + "!strip";
					require([ loadpath ], function(loadcontent) {
						main_content.innerHTML = loadcontent;
						// handle
						// widgets
						handleWidgets(orchestration);
					});
				};

				$scope.getFlashObject = function(key) {
					return flashParams[key];
				};

				$scope.getSiteObject = function(key) {
					return siteParams[key];
				};

				$scope.removeSiteObject = function(key) {
					siteParams[key] = undefined;
				};

				$scope.clearSiteObjects = function() {
					siteParams = {};
				};

				// expose to orchestration
				orchestrationManager.expose("navigateTo", $scope.navigate);
				orchestrationManager.expose("getFlashObject",
						$scope.getFlashObject);
				orchestrationManager.expose("getSiteObject",
						$scope.getSiteObject);
				orchestrationManager.expose("removeSiteObject",
						$scope.removeSiteObject);
				orchestrationManager.expose("clearSiteObjects",
						$scope.clearSiteObjects);

			});

			angular.bootstrap(document.getElementById('navigation'),
					[ 'navigation' ]);

			// initialize home content
			var main_content = document.getElementById('main_content');

			require([ loadpath ], function(loadcontent) {
				main_content.innerHTML = loadcontent;
				// handle widgets.
				handleWidgets(orchestration);
			});
		});