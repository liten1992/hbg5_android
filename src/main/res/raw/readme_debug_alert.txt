            MainScope().launch {
                delay(2000)

                val adapterList = mutableListOf<String>()
                for(index in 0 until 20) {
                    adapterList.add(index.toString())
                }
                val tag = "//DEBUG"

                // HBG5CheckOptionAlert
                if(false) {
                    v5ShowCheckOptionAlert(
                        request = HBG5CheckOptionAlert.DataRequest().also { alertRequest ->
                            alertRequest.title = "CheckOptionAlert"
                            alertRequest.optionList = adapterList.map { HBG5CheckOptionAlert.Item(checked = false, name = it) }
                        },
                        onYes = { selected ->
                            Log.d(tag, "HBG5CheckOptionAlert onYes: ${selected.map { it.toString() }.toLinkString(", ")}" )
                        },
                        onNo = {
                            Log.d(tag, "HBG5CheckOptionAlert onNo" )
                        }
                    )
                }
                // HBG5DateAlert
                if(false) {
                    v5ShowDateAlert(
                        request = HBG5DateAlert.DataRequest().also { alertRequest ->
                            alertRequest.date = HBG5Date()
                        },
                        onYes = { date ->
                            Log.d(tag, "HBG5DateAlert onYes: ${"%04d-%02d-%02d".format(date.year, date.month+1, date.day)}")
                        },
                        onNo = {
                            Log.d(tag, "HBG5DateAlert onNo" )
                        }
                    )
                }
                // HBG5EnumAlert
                if(false) {
                    v5ShowEnumAlert(
                        request = HBG5EnumAlert.DataRequest().also { alertRequest ->
                            alertRequest.fromView = uiLoginButton
                            alertRequest.optionList = adapterList
                        },
                        onYes = { index ->
                            Log.d(tag, "HBG5EnumAlert onYes: $index" )
                        },
                        onNo = {
                            Log.d(tag, "HBG5EnumAlert onNo" )
                        }
                    )
                }
                // HBG5IntentAlert
                if(false) {
                    v5ShowIntentAlert(
                        request = HBG5IntentAlert.DataRequest().also { alertRequest ->
                            alertRequest.title = "HBG5IntentAlert"
                            alertRequest.content = "功能測試"
                            alertRequest.contentAlignment = HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Center
                        },
                        onYes = {
                            Log.d(tag, "HBG5IntentAlert onYes" )
                        },
                        onNo = {
                            Log.d(tag, "HBG5IntentAlert onNo" )
                        }
                    )
                }
                // HBG5LoadingAlert
                if(false) {
                    v5ShowLoadingAlert()
                }
                // HBG5OptionAlert
                if(false) {
                    v5ShowOptionAlert(
                        request = HBG5OptionAlert.DataRequest().also { alertRequest ->
                            alertRequest.title = "HBG5OptionAlert"
                            alertRequest.optionList = adapterList
                        },
                        onYes = { index ->
                            Log.d(tag, "HBG5OptionAlert onYes: $index" )
                        },
                        onNo = {
                            Log.d(tag, "HBG5OptionAlert onNo" )
                        }
                    )
                }
                // HBG5TextAlert
                if(false) {
                    v5ShowTextAlert(
                        request = HBG5TextAlert.DataRequest().also { alertRequest ->
                            alertRequest.title = "HBG5TextAlert"
                            alertRequest.content = "功能測試"
                            alertRequest.contentAlignment = HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Center
                        },
                        onYes = {
                            Log.d(tag, "HBG5TextAlert onYes" )
                        }
                    )
                }
                // HBG5TextInputAlert
                if(false) {
                    v5ShowTextInputAlert(
                        request = HBG5TextInputAlert.DataRequest().also { alertRequest ->
                            alertRequest.title = "HBG5TextInputAlert"
                            alertRequest.input = "功能測試"
                            alertRequest.inputAlignment = HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Center
                        },
                        onYes = { text ->
                            Log.d(tag, "HBG5TextInputAlert onYes: $text" )
                        },
                        onNo = {
                            Log.d(tag, "HBG5TextInputAlert onNo" )
                        }
                    )
                }
                // HBG5TimeAlert
                if(true) {
                    v5ShowTimeAlert(
                        request = HBG5TimeAlert.DataRequest().also { alertRequest ->

                        },
                        onYes = { time ->
                            Log.d(tag, "HBG5TimeAlert onYes: ${"%02d:%02d".format(time.hour, time.minute)}")
                        },
                        onNo = {
                            Log.d(tag, "HBG5TimeAlert onNo")
                        }
                    )
                }
            }