define(
		[ 'contentEditor', 'dropzone' ],
		function(contentEditor, dropzone) {

			dropzone.autoDiscover = false;
			var imgDropzones = [];
			var initImgDropzones = function(widgetElement, fileParamName,
					fileHash) {
				var targetElement = widgetElement
						.querySelector("p[img-dropzone=" + fileParamName + "]");
				var options = {
					url : "app/fileupload",
					dictDefaultMessage : "将图片拖入该区域上传 或 点击这里上传图片 （只支持上传一张图片）",
					dictFallbackMessage : "请使用以下浏览器： Chrome 7+ / Firefox 4+ / IE 10+ / Opera 12+ / Safari 6+ ",
					dictInvalidFileType : "不支持的文件类型",
					dictFileTooBig : "文件太大",
					dictResponseError : "文件上传失败, 如需要请联系管理员",
					dictMaxFilesExceeded : "只可以添加一个文件",
					maxFilesize : 2,
					maxFiles : 1,
					headers : {
						"conversation-id" : fileHash
					},
					dictCancelUpload : "取消上传",
					acceptedFiles : "image/*",
					init : function() {
						this
								.on(
										"error",
										function(file) {
											this.removeFile(file);
											var customMessageArea = this.element.parentNode
													.querySelector("p[dz-custom-message='error']");
											customMessageArea
													.querySelector("span[message]").textContent = "上传失败";
											customMessageArea
													.querySelector("button[class='close']").onclick = function() {
												customMessageArea.setAttribute(
														"hidden", true);
											};
											customMessageArea
													.removeAttribute("hidden");
											this.src = undefined;
										});

						this
								.on(
										"success",
										function(file, data) {
											var customMessageArea = this.element.parentNode
													.querySelector("p[dz-custom-message='success']");
											customMessageArea
													.querySelector("span[message]").textContent = "上传成功";
											customMessageArea
													.querySelector("button[class='close']").onclick = function() {
												customMessageArea.setAttribute(
														"hidden", true);
											};
											customMessageArea
													.removeAttribute("hidden");
											this.disable();
											this.src = data.src;
										});
					}
				};
				var newDrop = new dropzone(targetElement, options);
				var customMessageAreas = newDrop.element.parentNode
						.querySelectorAll("p[dz-custom-message]");
				angular.forEach(customMessageAreas,
						function(customMessageArea) {
							customMessageArea.setAttribute("hidden", true);
						});
				newDrop.disable();
				imgDropzones.push(newDrop);
			};

			var linkManager = contentEditor.createLinkManager();
			linkManager.buttonGroup = [];

			return function($scope, secureDataRetriever, require,
					orchestration, widgetElement) {

				imgDropzones = [];

				var articleContentArea = widgetElement
						.querySelector("div.articleContentArea");

				var isSelectedValid = function() {
					return contentEditor.isSelectedValid(articleContentArea,
							"articleeditable");
				};

				var isLocationValid = function() {
					return contentEditor.isLocationValid(articleContentArea,
							"articleeditable");
				};

				var getSelectedParagraph = function() {
					return contentEditor.getLandMarkLocation(
							articleContentArea, "articleeditable");
				};

				var underlineBtn = null;
				var linethroughBtn = null;

				var processUnderlineApplied = function() {
					if (contentEditor.isUnderlineApplied()) {
						$(underlineBtn).addClass("active");
					} else {
						$(underlineBtn).removeClass("active");
					}
				};
				var processLineThroughApplied = function() {
					if (contentEditor.isLineThroughApplied()) {
						$(linethroughBtn).addClass("active");
					} else {
						$(linethroughBtn).removeClass("active");
					}
				};

				/*
				 * 1. each element in paragraph will also consists with: text,
				 * picUrl. text in paragraph will be HTML text. 2. each picture
				 * in picUrls will be put under paragraphs.
				 */
				$scope.article = {
					title : {
						text : undefined,
						picUrl : undefined
					},
					paragraphs : [],
					picUrls : []
				};

				$scope.initParagraphContent = [];

				$scope.restParagraphNum = 10;

				$scope.contentEditable = true;

				$scope.fontEditStart = function() {
					var result = false;
					if (isSelectedValid()) {
						result = true;
						processUnderlineApplied();
						processLineThroughApplied();
					}
					return result;
				};

				$scope.isInsertParagraphDisabled = false;

				$scope.insertNewParagraph = function() {
					if ($scope.restParagraphNum > 0) {
						var selectedParagraph = getSelectedParagraph();

						if (selectedParagraph === undefined) {
							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.push(newParagraph);
						} else {
							var index = parseInt(selectedParagraph
									.getAttribute("articleeditable"));

							var newParagraph = {
								text : '请在这里替换内容'
							};
							$scope.initParagraphContent.splice(index + 1, 0,
									newParagraph);
						}

						$scope.restParagraphNum--;
					}
					if ($scope.restParagraphNum === 0) {
						$scope.isInsertParagraphDisabled = true;
					}
				};

				var titlePicInsertMark = undefined;
				var totalPicNum = 11;

				var paraPicInsertStatus = [];
				for ( var i = 0; i < (totalPicNum - 1); i++) {
					paraPicInsertStatus.push(false);
				}

				$scope.isInsertPicDisabled = false;
				$scope.restPicNum = totalPicNum;

				$scope.picInsertStatus = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph === undefined) {
						if (titlePicInsertMark === undefined
								|| titlePicInsertMark === null) {
							$scope.isInsertPicDisabled = false;
						} else {
							$scope.isInsertPicDisabled = true;
						}
					} else {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicInsertMark === undefined
									|| titlePicInsertMark === null) {
								$scope.isInsertPicDisabled = false;
							} else {
								$scope.isInsertPicDisabled = true;
							}
						} else {
							$scope.isInsertPicDisabled = paraPicInsertStatus[index];
						}
					}
				};

				$scope.insertNewPicture = function() {
					var selectedParagraph = getSelectedParagraph();
					if (selectedParagraph !== undefined) {
						var index = parseInt(selectedParagraph
								.getAttribute("articleeditable"));
						if (index === -1) {
							if (titlePicInsertMark === undefined
									|| titlePicInsertMark === null) {
								titlePicInsertMark = "";
								var customMessageAreas = imgDropzones[0].element.parentNode
										.querySelectorAll("p[dz-custom-message]");
								angular.forEach(customMessageAreas, function(
										customMessageArea) {
									customMessageArea.setAttribute("hidden",
											true);
								});
								imgDropzones[0].removeAllFiles();
								imgDropzones[0].enable();
								$scope.restPicNum--;
								$scope.isInsertPicDisabled = true;
							}
						} else {
							if (!paraPicInsertStatus[index]) {
								paraPicInsertStatus[index] = true;
								var customMessageAreas = imgDropzones[index + 1].element.parentNode
										.querySelectorAll("p[dz-custom-message]");
								angular.forEach(customMessageAreas, function(
										customMessageArea) {
									customMessageArea.setAttribute("hidden",
											true);
								});
								imgDropzones[index + 1].removeAllFiles();
								imgDropzones[index + 1].enable();
								$scope.restPicNum--;
							}
						}
					} else {
						if (titlePicInsertMark === undefined
								|| titlePicInsertMark === null) {
							titlePicInsertMark = "";
							var customMessageAreas = imgDropzones[0].element.parentNode
									.querySelectorAll("p[dz-custom-message]");
							angular.forEach(customMessageAreas, function(
									customMessageArea) {
								customMessageArea.setAttribute("hidden", true);
							});
							imgDropzones[0].removeAllFiles();
							imgDropzones[0].enable();
							$scope.restPicNum--;
							$scope.isInsertPicDisabled = true;
						}
					}
					if ($scope.restPicNum <= 0) {
						$scope.isInsertPicDisabled = true;
					}
				};

				$scope.displayTitlePicZone = function() {
					var result = false;
					if (titlePicInsertMark !== undefined
							&& titlePicInsertMark !== null) {
						result = true;
					}
					return result;
				};

				$scope.cancelTitlePicZone = function() {
					var file = imgDropzones[0].files[0];
					var fileName = undefined;
					if (file !== undefined && file !== null) {
						fileName = file.name;
					}
					if (fileName !== undefined) {
						secureDataRetriever.setData({
							fileName : fileName
						});

						secureDataRetriever.setHeader({
							"conversation-id" : $scope.conversation_id,
						});

						secureDataRetriever.onSuccess(function(data) {
							if (data.status === true) {
								titlePicInsertMark = undefined;
								$scope.isInsertPicDisabled = false;
								imgDropzones[0].disable();
								imgDropzones[0].removeAllFiles();
								imgDropzones[0].src = undefined;
								$scope.restPicNum++;

								if (!$scope.$$phase) {
									$scope.$apply();
								}
							}
						});

						secureDataRetriever.post('app/cancelupload');

					} else {
						titlePicInsertMark = undefined;
						$scope.isInsertPicDisabled = false;
						imgDropzones[0].disable();
						imgDropzones[0].removeAllFiles();
						$scope.restPicNum++;
					}
				};

				$scope.displayParagraphPicZone = function(index) {
					return paraPicInsertStatus[index];
				};

				$scope.cancelParagraphPicZone = function(index) {
					var file = imgDropzones[index + 1].files[0];
					var fileName = undefined;
					if (file !== undefined && file !== null) {
						fileName = file.name;
					}
					if (fileName !== undefined) {

						secureDataRetriever.setData({
							fileName : fileName
						});

						secureDataRetriever.setHeader({
							"conversation-id" : $scope.conversation_id,
						});

						secureDataRetriever.onSuccess(function(data) {
							if (data.status === true) {
								imgDropzones[index + 1].disable();
								imgDropzones[index + 1].removeAllFiles();
								imgDropzones[index + 1].src = undefined;
								paraPicInsertStatus[index] = false;
								$scope.restPicNum++;

								if (!$scope.$$phase) {
									$scope.$apply();
								}
							}
						});

						secureDataRetriever.post('app/cancelupload');

					} else {
						imgDropzones[index + 1].disable();
						imgDropzones[index + 1].removeAllFiles();
						paraPicInsertStatus[index] = false;
						$scope.restPicNum++;
					}
				};

				$scope.registerKeepConversationModel = function(element,
						context) {
					$scope.conversationModelElement = element[0];

					$($scope.conversationModelElement).on("hidden", function() {
						if ($scope.conversationInvalid === true) {
							$scope.endConversation();
						} else if ($scope.conversationInvalid !== false) {
							$($scope.conversationModelElement).modal("show");
						}
					});

					orchestration.expose("confirmUploadConversation", function(
							uid) {
						$scope.conversationRemainTime = 60;
						$scope.conversationID = uid;

						$($scope.conversationModelElement).modal("show");

						var int = setInterval(function() {
							if ($scope.conversationRemainTime > 0) {
								$scope.conversationRemainTime--;
								if (!$scope.$$phase) {
									$scope.$apply();
								}
							} else {
								clearInterval(int);
								$scope.conversationInvalid = true;
								$($scope.conversationModelElement)
										.modal("hide");
							}
						}, 1000);
					});
				};

				$scope.keepConversation = function() {
					$scope.conversationInvalid = false;
					secureDataRetriever.setData({
						"conversation-id" : $scope.conversationID
					});
					secureDataRetriever.onSuccess(function() {
						$($scope.conversationModelElement).modal("hide");
					});
					secureDataRetriever.post("app/keepUploadConversation");
				};

				$scope.endConversation = function() {
					orchestration.invoke("navigation", "getFlashObject",
							"confirm_conversation_interval", function(
									confirm_conversation_interval) {
								clearInterval(confirm_conversation_interval);
								var options = {
									location : 'information.html'
								};
								orchestration.invoke('navigation',
										'navigateTo', options);
							});
				};

				$scope.endConversationFromConfirm = function() {
					$scope.conversationInvalid = true;
					$($scope.conversationModelElement).modal("hide");
				};

				orchestration.invoke("navigation", "getFlashObject",
						"new_article_title", function(articleTitle) {
							$scope.article.title.text = articleTitle;
						});

				orchestration.invoke("navigation", "getSiteObject", "userId",
						function(userId) {
							$scope.article.userId = userId;
						});

				orchestration
						.invoke(
								"navigation",
								"getFlashObject",
								"conversation_id",
								function(conversation_id) {
									$scope.conversation_id = conversation_id;

									orchestration
											.invoke(
													"navigation",
													"getFlashObject",
													"confirm_conversation_interval",
													function(
															confirm_conversation_interval) {

														orchestration
																.invoke(
																		"navigation",
																		"registerCleanupExecute",
																		function(
																				execute) {
																			secureDataRetriever
																					.setData({
																						"conversation-id" : $scope.conversation_id,
																					});

																			secureDataRetriever
																					.onSuccess(function(
																							data) {
																						clearInterval(confirm_conversation_interval);
																						execute();
																						return false;
																					});

																			secureDataRetriever
																					.onFailure(function(
																							data) {
																						clearInterval(confirm_conversation_interval);
																						execute();
																						return false;
																					});

																			secureDataRetriever
																					.post('app/endUploadConversation');
																		});
													});
								});

				$scope.bold = function() {
					if (isSelectedValid()) {
						contentEditor.boldText();
					}
				};

				$scope.italic = function() {
					if (isSelectedValid()) {
						contentEditor.italicText();
					}
				};

				$scope.underline = function() {
					if (isSelectedValid()) {
						contentEditor.underlineText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.lineThrough = function() {
					if (isSelectedValid()) {
						contentEditor.lineThroughText();
						processUnderlineApplied();
						processLineThroughApplied();
					}
				};

				$scope.registerToLinkManager = function(element, context) {
					linkManager.linkMenuElement = element[0];
				};

				$scope.registerModelToLinkManager = function(element, context) {
					linkManager.addLinkModel = element[0];
				};

				$scope.createLinkStartValidateMark = false;

				var resetCreateLinkModel = function() {
					$scope.createLinkStartValidateMark = false;
					$scope.linkTitle = null;
					$scope.linkUrl = null;
				};

				$scope.openCreateLinkModel = function() {
					contentEditor.saveSelection();
					resetCreateLinkModel();
					$(linkManager.addLinkModel).modal('show');
				};

				$scope.checkIfLinkCreationInvalid = function() {
					return $scope.createLinkStartValidateMark
							&& ($scope.linkCreationForm.linkTitle.$error.required
									|| $scope.linkCreationForm.linkUrl.$error.required || $scope.linkCreationForm.linkUrl.$error.url);
				};

				$scope.initLinkStatus = function() {
					linkManager.buttonGroup.forEach(function(button) {
						var title = button.title;
						if (linkManager.isLinkApplied(title)) {
							$(button).addClass("active");
						} else {
							$(button).removeClass("active");
						}
					});
				};

				$scope.createLink = function() {
					$scope.createLinkStartValidateMark = true;
					var result = $scope.checkIfLinkCreationInvalid();
					if (result !== true) {
						contentEditor.restoreSelection();

						var linkTitle = $scope.linkTitle;
						var linkUrl = $scope.linkUrl;
						linkManager.createLink(linkTitle, linkUrl);

						var li = document.createElement("li");
						var button = document.createElement("button");
						button.setAttribute("type", "button");
						button.setAttribute("class", "btn btn-primary");
						button.setAttribute("data-toggle", "button");
						button.innerHTML = linkTitle;
						button.onclick = function() {
							if (linkManager.isLinkApplied(linkTitle)) {
								linkManager.unLink(linkTitle);
								$(button).removeClass("active");
							} else {
								linkManager.applyLink(linkTitle);
								$(button).addClass("active");
							}
						};
						li.appendChild(button);
						linkManager.linkMenuElement.appendChild(li);
						// register the button to linkMenu with titleName.
						button.title = linkTitle;
						linkManager.buttonGroup.push(button);
						button.onclick();
						$(linkManager.addLinkModel).modal('hide');
					}
				};

				$scope.overParagraphPanelIndex = -1;
				$scope.selectedParagraph = -1;

				$scope.checkOverParagrahIndex = function(index) {
					return $scope.overParagraphPanelIndex === index;
				};

				$scope.selectParagraph = function(index, $event) {
					$scope.selectedParagraph = index;

					// disable pic insert button.
					if (index === -1 && titlePicInsertMark !== undefined
							&& titlePicInsertMark !== null) {
						$scope.isInsertPicDisabled = true;
					}

					// stop event bubble up.
					if ($event.stopPropagation) {
						$event.stopPropagation();
					}
					if ($event.preventDefault) {
						$event.preventDefault();
					}
					$event.cancelBubble = true;
					$event.returnValue = false;
				};

				$scope.checkSelectPanel = function(index) {
					return ($scope.selectedParagraph === index);
				};

				$scope.displayParapgrahPanel = function(index) {
					return (checkSelectPanel(index) || checkOverParagrahIndex(index));
				};

				$scope.enterParagraph = function(index) {
					$scope.overParagraphPanelIndex = index;
				};

				$scope.leaveParagraph = function() {
					$scope.overParagraphPanelIndex = -1;
				};

				$scope.enterCrossClass = "icon-remove-circle";

				$scope.enterCross = function() {
					$scope.enterCrossClass = "icon-remove-sign";
				};

				$scope.leaveCross = function() {
					$scope.enterCrossClass = "icon-remove-circle";
				};

				$scope.removeParagraph = function(index) {
					$scope.initParagraphContent.splice(index, 1);
					if ($scope.restParagraphNum < 10) {
						$scope.restParagraphNum++;
						$scope.isInsertParagraphDisabled = false;
						// remove picture in paragraph
						if (paraPicInsertStatus[index]) {
							$scope.cancelParagraphPicZone(index);
						}
					}
				};

				$scope.initAbstractTitleImage = function() {
					var fileHash = $scope.conversation_id;
					var fileName = "abstract_title_image";
					initImgDropzones(widgetElement, fileName, fileHash);
				};

				$scope.preProcessImageDropzone = function(index, paragraph) {
					var fileHash = $scope.conversation_id;
					var fileName = "upload_img_file_" + index;
					paragraph.imgdesc = {};
					paragraph.imgdesc.hash = fileHash;
					paragraph.imgdesc.name = fileName;
				};

				$scope.postProcessImageDropzone = function(element, context) {
					var fileParamName = $scope.initParagraphContent[context.$index].imgdesc.name;
					var fileHash = $scope.initParagraphContent[context.$index].imgdesc.hash;
					initImgDropzones(widgetElement, fileParamName, fileHash);
				};

				$scope.cancelArticle = function() {
					var options = {
						location : 'information.html'
					};
					orchestration.invoke('navigation', 'navigateTo', options);
				};

				$scope.submitArticle = function() {

					secureDataRetriever.setData({
						"conversation-id" : $scope.conversation_id,
					});

					secureDataRetriever
							.onSuccess(function(data) {
								if (data.status === true) {
									// TODO: to finish.
									if (imgDropzones.length > 0) {
										if (imgDropzones[0].src === undefined
												|| imgDropzones[0].src == null) {
											imgDropzones[0].src == "";
										}
										$scope.article.title.picUrl = imgDropzones[0].src;
										angular.forEach(imgDropzones, function(
												value, key) {
											if (key !== 0) {
												if (value.src === undefined
														|| value.src === null) {
													value.src = "";
												}
												$scope.article.picUrls
														.push(value.src);
											}
										});
									}
									var paragraphs = document
											.querySelectorAll("div[articleeditable]>p[contenteditable]");
									angular.forEach(paragraphs, function(value,
											key) {
										$scope.article.paragraphs
												.push(value.innerHTML);
									});

									secureDataRetriever.setData(JSON
											.stringify($scope.article));

									secureDataRetriever.onSuccess(function(
											response) {
										if (response.status === true) {
											// TODO: on success.

										} else {
											// TODO: on operational failure.
										}
									});

									secureDataRetriever.onFailure(function() {
										// TODO: on failure.
									});

									secureDataRetriever.post(
											"infomgmt/saveNewArticle",
											"application/json; charset=UTF-8");

								} else {
									// complete conversation fail process.
								}
							});

					secureDataRetriever.post('app/completeUploadConversation');
				};

				return function(widgetElement) {
					underlineBtn = widgetElement
							.querySelector("button[underline-btn]");
					linethroughBtn = widgetElement
							.querySelector("button[linethrough-btn]");
				};
			};
		});