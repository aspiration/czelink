define(
		[ 'jquery', 'require', 'orchestration', 'secureDataRetriever' ],
		function(jquery, require, orchestration, secureDataRetriever) {

			// flash repository.
			var flashParams = {};
			// site repository.
			var siteParams = {};

			var currentRole = "ROLE_ANONYMOUS";

			// prepare loadpath:
			var location = window.location.hash;
			var search = window.location.search;

			if (search !== undefined && search !== null && search !== "") {
				search = search.substring(1, search.length);
				var searchParam = search.split("=");
				if (searchParam.length === 2 && searchParam[0] === "activated") {
					siteParams['activated'] = searchParam[1];
				}
			}

			var configuration = {
				type : "post",
				dataType : "json",
				url : "app/navigationList",
			};

			if (siteParams['activated'] !== undefined
					&& siteParams['activated'] !== null
					&& siteParams['activated'] !== "") {
				configuration.data = {
					activateInstance : siteParams['activated']
				};
			} else {
				configuration.data = {
					activateInstance : ""
				};
			}

			configuration.success = function(data) {
				if (data.status) {
					var navList = data.navigationList;
					currentRole = data.role;
					if (data.verifyKey !== undefined && data.verifyKey !== null
							&& data.verifyKey !== "") {
						siteParams['verifyKey'] = data.verifyKey;
					}

					siteParams["userId"] = data.uid;

					var candidates = [];

					navList.forEach(function(item) {
						candidates.push(item.hashLink);
					});
					candidates.push("#help");

					var loadpath = "text!views/home.html";
					if (candidates.indexOf(location) > -1) {
						var target = location.slice(1, location.length);
						loadpath = "text!views/" + target + ".html";
					} else {
						window.location.hash = "#home";
					}

					var evalRenderOnlyExpr = function(renderOnlyKey) {
						var renderOnlyValue = false;

						if (renderOnlyKey.indexOf("!") === 0) {
							renderOnlyKey = renderOnlyKey.slice(1,
									renderOnlyKey.length);
							renderOnlyValue = flashParams[renderOnlyKey];
							if (renderOnlyValue === null
									|| renderOnlyValue === undefined) {
								renderOnlyValue = siteParams[renderOnlyKey];
							}
							if (renderOnlyValue === null
									|| renderOnlyValue === undefined) {
								renderOnlyValue = false;
							}
							renderOnlyValue = !renderOnlyValue;
						} else {
							renderOnlyValue = flashParams[renderOnlyKey];
							if (renderOnlyValue === null
									|| renderOnlyValue === undefined) {
								renderOnlyValue = siteParams[renderOnlyKey];
							}
						}
						return renderOnlyValue;
					};

					var initWidgetApplication = function(widgetName,
							widgetElement, orchestration) {
						var widgetModule = angular.module(widgetName, []);

						// add post render directive
						widgetModule
								.directive(
										"ngPostRender",
										[
												'$timeout',
												function(timeout) {
													return function(scope,
															element, attrs) {
														timeout(
																function() {
																	var postRenderMethodName = attrs["ngPostRender"];
																	scope[postRenderMethodName]
																			(
																					element,
																					scope);
																}, 0);
													};
												} ]);

						var outerNgAppCallback = undefined;

						var widgetControllerPath = 'widgets/js/' + widgetName;
						require(
								[ widgetControllerPath ],
								function(widgetController) {
									// define controller
									widgetModule
											.controller(
													widgetName + "Ctrl",
													function($scope) {
														// register
														// to
														// orchestration.
														var orchestrationManager = orchestration(
																widgetName,
																$scope);

														orchestrationManager.replaceWith = function(
																targetWidgetName) {
															var widget = document
																	.querySelectorAll('div[widget='
																			+ targetWidgetName
																			+ ']');

															// get
															// widget
															// template
															var widgetName = widget
																	.getAttribute('widget');
															// attach
															// controller
															widget
																	.setAttribute(
																			'ng-controller',
																			widgetName
																					+ 'Ctrl');
															var widgetPath = 'text!widgets/views/'
																	+ widgetName
																	+ ".html!strip";
															require(
																	[ widgetPath ],
																	function(
																			widgetTemplate) {

																		// security
																		// process.
																		var widgetInnerHTML = $
																				.parseHTML(widgetTemplate)[0];
																		var securedElems = widgetInnerHTML
																				.querySelectorAll("[secure]");

																		angular
																				.forEach(
																						securedElems,
																						function(
																								elem) {

																							var secureRoles = elem
																									.getAttribute("secure");
																							if (secureRoles
																									.indexOf(currentRole) === -1) {
																								elem.outerHTML = "";
																							}
																						});
																		widget.innerHTML = widgetInnerHTML.outerHTML;

																		initWidgetApplication(
																				widgetName,
																				widget,
																				orchestration);
																	});
														};

														// handled
														// by
														// consumer.
														outerNgAppCallback = widgetController(
																$scope,
																new secureDataRetriever(
																		widgetElement),
																require,
																orchestrationManager,
																widgetElement);
													});
									angular.bootstrap(widgetElement,
											[ widgetName ]);

									// outer angular application
									// call - usually for dom
									// manipulate.
									if (outerNgAppCallback !== undefined) {
										outerNgAppCallback(widgetElement);
									}

									// show the widget element
									// after rendering.
									widgetElement.removeAttribute("hidden");
								});
					};

					var handleWidgets = function(orchestration) {
						var widgets = document.querySelectorAll('div[widget]');

						angular
								.forEach(
										widgets,
										function(widget) {
											// hidden the widget
											// during rendering.
											widget.setAttribute("hidden", true);

											var widgetName = widget
													.getAttribute('widget');

											// get widget render
											// condition.
											var renderOnlyKey = widget
													.getAttribute('renderOnly');
											var renderOnlyValue = false;

											if (renderOnlyKey !== null
													&& renderOnlyKey !== undefined) {
												var renderOnlyParamKeys = renderOnlyKey
														.split("&&");
												renderOnlyValue = true;

												renderOnlyParamKeys
														.forEach(function(
																renderOnlyParamKey) {
															renderOnlyParamKey = renderOnlyParamKey
																	.trim();
															renderOnlyValue = renderOnlyValue
																	&& evalRenderOnlyExpr(renderOnlyParamKey);
														});
											}

											// get widget
											// template
											if (renderOnlyValue === true
													|| renderOnlyKey === null
													|| renderOnlyKey === undefined) {
												// attach
												// controller
												widget.setAttribute(
														'ng-controller',
														widgetName + 'Ctrl');
												var widgetPath = 'text!widgets/views/'
														+ widgetName
														+ ".html!strip";
												require(
														[ widgetPath ],
														function(widgetTemplate) {

															// security
															// process.
															var widgetInnerHTML = $
																	.parseHTML(widgetTemplate)[0];
															var securedElems = widgetInnerHTML
																	.querySelectorAll("[secure]");

															angular
																	.forEach(
																			securedElems,
																			function(
																					elem) {
																				var secureRoles = elem
																						.getAttribute("secure");
																				if (secureRoles
																						.indexOf(currentRole) === -1) {
																					elem.outerHTML = "";
																				}
																			});
															widget.innerHTML = widgetInnerHTML.outerHTML;

															initWidgetApplication(
																	widgetName,
																	widget,
																	orchestration);
														});
											}
										});

					};

					// initialize navigation
					var ngNavbar = angular.module('navigation', []);

					ngNavbar
							.controller(
									'navCtrl',
									function($scope) {

										var orchestrationManager = orchestration(
												'navigation', $scope);

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

										// options: location,
										// flashObjs, siteObjs
										$scope.navigate = function(options) {
											var execute = function() {
												flashParams = {};

												var location = options.location;
												var flashObjs = options.flashObjs;
												var siteObjs = options.siteObjs;

												$scope.currentHash = "#"
														+ location
																.slice(
																		0,
																		location
																				.indexOf(".html"));

												if (!$scope.$$phase) {
													$scope.$apply();
												}

												if (flashObjs !== null
														&& flashObjs !== undefined) {
													flashParams = flashObjs;
												}
												flashParams['currentLocation'] = location;

												if (siteObjs !== null
														&& siteObjs !== undefined) {
													siteParams = angular
															.extend({},
																	siteParams,
																	siteObjs);
												}

												var main_content = document
														.getElementById('main_content');
												main_content.setAttribute(
														"hidden", true);
												var loadpath = "text!views/"
														+ location + "!strip";
												require(
														[ loadpath ],
														function(loadcontent) {
															main_content.innerHTML = loadcontent;
															// handle
															// widgets
															handleWidgets(orchestration);
															main_content
																	.removeAttribute("hidden");
														});
											};

											var cleanup = flashParams['__cleanupExtension__'];
											if (cleanup instanceof Function) {
												cleanup(execute);
											} else {
												execute();
											}
										};

										$scope.setCleanupExtension = function(
												extension) {
											if (extension instanceof Function) {
												flashParams['__cleanupExtension__'] = extension;
											}
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

										var restoreFlashObjects = function() {
											return angular.copy(flashParams);
										};

										var restoreSiteObjects = function() {
											return angular.copy(siteParams);
										};

										$scope.refreshStatus = function(options) {

											var flashObjs = options.flashObjs;
											var siteObjs = options.siteObjs;

											flashParams = angular.extend({},
													flashParams, flashObjs);

											siteParams = angular.extend({},
													siteParams, siteObjs);

											siteParams['activated'] = undefined;
											siteParams['verifyKey'] = undefined;

											var restoredFlashObjects = restoreFlashObjects();
											var restoredSiteObjects = restoreSiteObjects();

											$
													.getJSON(
															'app/getCurrentRole',
															function(data) {
																currentRole = data.role;
																var currentLocation = flashParams['currentLocation'];

																var options = {
																	location : currentLocation,
																	flashObjs : restoredFlashObjects,
																	siteObjs : restoredSiteObjects
																};

																$scope
																		.navigate(options);
															});
										};

										// expose to
										// orchestration
										orchestrationManager.expose(
												"registerCleanupExecute",
												$scope.setCleanupExtension);
										orchestrationManager.expose(
												"navigateTo", $scope.navigate);
										orchestrationManager.expose(
												"getFlashObject",
												$scope.getFlashObject);
										orchestrationManager.expose(
												"getSiteObject",
												$scope.getSiteObject);
										orchestrationManager.expose(
												"removeSiteObject",
												$scope.removeSiteObject);
										orchestrationManager.expose(
												"clearSiteObjects",
												$scope.clearSiteObjects);
										orchestrationManager.expose(
												"refreshStatus",
												$scope.refreshStatus);
										orchestrationManager
												.expose(
														"getActivatedInstance",
														function() {
															return siteParams['activated'];
														});
										orchestrationManager
												.expose(
														"getVerifyKey",
														function() {
															return siteParams['verifyKey'];
														});

										// build navItems
										var navItems = [];

										navList
												.forEach(function(targetItem) {
													var navItem = {
														ngClass : function() {
															return $scope
																	.activityCheck(targetItem.hashLink);
														},
														href : targetItem.hashLink,
														label : targetItem.label
													};

													// build on
													// click
													// function
													// TODO: may
													// change in
													// the
													// future
													if (targetItem.hashLink === "#information") {
														navItem.ngClick = function() {
															$scope
																	.navigate({
																		location : 'information.html',
																		flashObjs : {
																			info_abstract : true
																		}
																	});
														};
													} else {
														var location = targetItem.hashLink
																.slice(
																		1,
																		targetItem.hashLink.length)
																+ ".html";
														navItem.ngClick = function() {
															$scope
																	.navigate({
																		location : location
																	});
														};
													}
													navItems.push(navItem);
												});
										$scope.navItems = navItems;
									});

					angular.bootstrap(document.getElementById('navigation'),
							[ 'navigation' ]);
					document.getElementById('navigation').removeAttribute(
							"hidden");

					// initialize home content
					var main_content = document.getElementById('main_content');
					main_content.setAttribute("hidden", true);
					flashParams['currentLocation'] = "home.html";

					require([ loadpath ], function(loadcontent) {
						main_content.innerHTML = loadcontent;
						// handle widgets.
						handleWidgets(orchestration);
						main_content.removeAttribute("hidden");
					});
				}
			};
			$.ajax(configuration);
		});