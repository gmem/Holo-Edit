{
	"patcher" : 	{
		"fileversion" : 1,
		"rect" : [ 294.0, 44.0, 799.0, 676.0 ],
		"bglocked" : 0,
		"defrect" : [ 294.0, 44.0, 799.0, 676.0 ],
		"openrect" : [ 0.0, 0.0, 0.0, 0.0 ],
		"openinpresentation" : 0,
		"default_fontsize" : 12.0,
		"default_fontface" : 0,
		"default_fontname" : "Arial",
		"gridonopen" : 0,
		"gridsize" : [ 15.0, 15.0 ],
		"gridsnaponopen" : 0,
		"toolbarvisible" : 1,
		"boxanimatetime" : 200,
		"imprint" : 0,
		"boxes" : [ 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 11.595187,
					"id" : "obj-116",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 714.0, 56.5, 41.0, 20.0 ],
					"text" : "* 100."
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 11.595187,
					"id" : "obj-117",
					"maxclass" : "newobj",
					"numinlets" : 0,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 707.0, 33.5, 25.0, 20.0 ],
					"text" : "r tr"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-118",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "signal" ],
					"patching_rect" : [ 436.0, 57.0, 52.0, 17.0 ],
					"text" : "receive~ s"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-1",
					"items" : [ "transmode", "time", ",", "transmode", "freq", ",", "transmode", "auto" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 135.0, 110.0, 82.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-2",
					"items" : [ "envpres", "off", ",", "envpres", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 153.0, 129.0, 64.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-3",
					"linecount" : 2,
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 2,
					"outlettype" : [ "signal", "signal" ],
					"patcher" : 					{
						"fileversion" : 1,
						"rect" : [ 141.0, 123.0, 308.0, 378.0 ],
						"bglocked" : 0,
						"defrect" : [ 141.0, 123.0, 308.0, 378.0 ],
						"openrect" : [ 0.0, 0.0, 0.0, 0.0 ],
						"openinpresentation" : 0,
						"default_fontsize" : 12.0,
						"default_fontface" : 0,
						"default_fontname" : "Arial",
						"gridonopen" : 0,
						"gridsize" : [ 15.0, 15.0 ],
						"gridsnaponopen" : 0,
						"toolbarvisible" : 1,
						"boxanimatetime" : 200,
						"imprint" : 0,
						"boxes" : [ 							{
								"box" : 								{
									"fontname" : "Arial",
									"fontsize" : 9.0,
									"id" : "obj-1",
									"maxclass" : "newobj",
									"numinlets" : 2,
									"numoutlets" : 1,
									"outlettype" : [ "signal" ],
									"patching_rect" : [ 133.0, 165.0, 40.0, 17.0 ],
									"text" : "==~ 0."
								}

							}
, 							{
								"box" : 								{
									"fontname" : "Arial",
									"fontsize" : 9.0,
									"id" : "obj-2",
									"maxclass" : "newobj",
									"numinlets" : 2,
									"numoutlets" : 1,
									"outlettype" : [ "signal" ],
									"patching_rect" : [ 93.0, 191.0, 50.0, 17.0 ],
									"text" : "*~"
								}

							}
, 							{
								"box" : 								{
									"fontname" : "Arial",
									"fontsize" : 9.0,
									"id" : "obj-3",
									"maxclass" : "newobj",
									"numinlets" : 6,
									"numoutlets" : 1,
									"outlettype" : [ "signal" ],
									"patching_rect" : [ 133.0, 142.0, 89.0, 17.0 ],
									"text" : "biquad~ 1 1 1 0 0"
								}

							}
, 							{
								"box" : 								{
									"comment" : "",
									"id" : "obj-4",
									"maxclass" : "outlet",
									"numinlets" : 1,
									"numoutlets" : 0,
									"patching_rect" : [ 46.0, 272.0, 15.0, 15.0 ]
								}

							}
, 							{
								"box" : 								{
									"comment" : "",
									"id" : "obj-5",
									"maxclass" : "outlet",
									"numinlets" : 1,
									"numoutlets" : 0,
									"patching_rect" : [ 232.0, 272.0, 15.0, 15.0 ]
								}

							}
, 							{
								"box" : 								{
									"comment" : "",
									"id" : "obj-6",
									"maxclass" : "inlet",
									"numinlets" : 0,
									"numoutlets" : 1,
									"outlettype" : [ "signal" ],
									"patching_rect" : [ 46.0, 56.0, 15.0, 15.0 ]
								}

							}
, 							{
								"box" : 								{
									"comment" : "",
									"id" : "obj-7",
									"maxclass" : "inlet",
									"numinlets" : 0,
									"numoutlets" : 1,
									"outlettype" : [ "" ],
									"patching_rect" : [ 232.0, 56.0, 15.0, 15.0 ]
								}

							}
 ],
						"lines" : [ 							{
								"patchline" : 								{
									"destination" : [ "obj-5", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-2", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-5", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-7", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-2", 1 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-1", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-1", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-3", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-3", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-7", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-2", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-6", 0 ]
								}

							}
, 							{
								"patchline" : 								{
									"destination" : [ "obj-4", 0 ],
									"hidden" : 0,
									"midpoints" : [  ],
									"source" : [ "obj-6", 0 ]
								}

							}
 ]
					}
,
					"patching_rect" : [ 46.0, 487.0, 36.0, 27.0 ],
					"saved_object_attributes" : 					{
						"default_fontname" : "Arial",
						"fontname" : "Arial",
						"default_fontsize" : 12.0,
						"fontface" : 0,
						"fontsize" : 12.0,
						"globalpatchername" : "",
						"default_fontface" : 0
					}
,
					"text" : "p auto mono"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-4",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 100.0, 470.0, 72.0, 17.0 ],
					"text" : "loadmess 120"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-5",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 292.0, 68.0, 77.0, 17.0 ],
					"text" : "loadmess set 1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-6",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 602.0, 407.0, 17.0 ],
					"text" : "sinmode <'vocoder'|'additive': mode> configure sinusoids remix mode ['vocoder']"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-7",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 550.0, 353.0, 17.0 ],
					"text" : "envmode <'trueenv'|'lpc': mode> ... set envelope estimation mode ['trueenv']"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-8",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 576.0, 326.0, 17.0 ],
					"text" : "lpcorder <num: order> ... set order for LPC envelope estimation [12]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-9",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 537.0, 542.0, 17.0 ],
					"text" : "envscale <'on'|'off': switch> or envscale <num: timbre> [<num: mean>] ... configure or set envelope scaling [off, 1 1]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-10",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 511.0, 320.0, 17.0 ],
					"text" : "envpres <'on'|'off': switch> ... configure envelope preservation [off]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-11",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 524.0, 555.0, 17.0 ],
					"text" : "envtrans <'on'|'off': switch> or <num: cent> ... configure or set envelope transformation (requires transpose on) [off, 0]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-12",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 498.0, 424.0, 17.0 ],
					"text" : "transpose <'on'|'off': switch> or <num: cent> ... configure or set transposition [on, 0]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-13",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 589.0, 455.0, 17.0 ],
					"text" : "remix <'on'|'off': switch> ... configure or set sinusoids/noise/transients remix [off, 1 1 1 0 0.1]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-14",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 563.0, 342.0, 17.0 ],
					"text" : "maxfreq <num: hz> ... set maximum f0 for true envelope estimation [500]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-15",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 485.0, 282.0, 17.0 ],
					"text" : "maxtrans <num: cent> ... set maximum transposition [1200]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-16",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 472.0, 364.0, 17.0 ],
					"text" : "transmode <'time'|'auto'|'freq': switch> ... configure transposition mode [time]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-17",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 433.0, 417.0, 17.0 ],
					"text" : "transients <'on'|'off': switch> ... configure transient preservation (also for remixing) [off]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-18",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 459.0, 434.0, 17.0 ],
					"text" : "shapeinv <'on'|'off': switch> ... configure wave form preservation (overrides transients) [off]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-19",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 446.0, 320.0, 17.0 ],
					"text" : "stereopres <'on'|'off': switch> ... configure stereo preservation [off]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-20",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 420.0, 167.0, 89.0, 17.0 ],
					"text" : "loadmess set 200"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-21",
					"items" : [ "envmode", "trueenv", ",", "envmode", "lpc" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 205.0, 167.0, 88.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-22",
					"items" : [ "envscale", "off", ",", "envscale", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 226.0, 148.0, 67.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-23",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 565.0, 148.0, 72.0, 17.0 ],
					"text" : "loadmess 100"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-24",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 420.0, 186.0, 83.0, 17.0 ],
					"text" : "loadmess set 12"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-25",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 186.0, 69.0, 15.0 ],
					"text" : "lpcorder $1"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"id" : "obj-26",
					"maxclass" : "number",
					"numinlets" : 1,
					"numoutlets" : 2,
					"outlettype" : [ "int", "bang" ],
					"patching_rect" : [ 372.0, 186.0, 46.0, 17.0 ],
					"triscale" : 0.9
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-27",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 421.0, 187.0, 204.0, 17.0 ],
					"text" : "LPC order for spectral envelope estimation"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-28",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 565.0, 149.0, 199.0, 17.0 ],
					"text" : "envelope scaling (timbre and mean factor)"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"id" : "obj-29",
					"maxclass" : "flonum",
					"numinlets" : 1,
					"numoutlets" : 2,
					"outlettype" : [ "float", "bang" ],
					"patching_rect" : [ 382.0, 148.0, 36.0, 17.0 ],
					"triscale" : 0.9
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-30",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 148.0, 79.0, 15.0 ],
					"text" : "envscale $1 $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-31",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 431.0, 148.0, 41.0, 17.0 ],
					"text" : "* 0.01"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-32",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 167.0, 69.0, 15.0 ],
					"text" : "maxfreq $1"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"id" : "obj-33",
					"maxclass" : "flonum",
					"numinlets" : 1,
					"numoutlets" : 2,
					"outlettype" : [ "float", "bang" ],
					"patching_rect" : [ 372.0, 167.0, 46.0, 17.0 ],
					"triscale" : 0.9
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-34",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 421.0, 168.0, 302.0, 17.0 ],
					"text" : "maximal fundamental frequency for spectral envelope estimation"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-35",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 420.0, 148.0, 143.0, 15.0 ],
					"size" : 201.0
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-36",
					"items" : [ "sinmode", "vocoder", ",", "sinmode", "additive" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 206.0, 244.0, 88.0, 17.0 ],
					"textcolor" : [ 0.65098, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 18.0,
					"id" : "obj-37",
					"linecount" : 7,
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "signal" ],
					"patching_rect" : [ 46.0, 244.0, 151.0, 151.0 ],
					"text" : "supervp.trans~ 1 2250 0 4 @transients on @shapeinv on @envtrans on @maxfreq 200 @remix on"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 12.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-38",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 333.0, 88.0, 112.0, 20.0 ],
					"text" : "real-time control"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-39",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 220.0, 288.0, 81.0, 17.0 ],
					"text" : "sinusoids, noise"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-40",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 490.0, 128.0, 72.0, 17.0 ],
					"text" : "loadmess 120"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-41",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 490.0, 110.0, 72.0, 17.0 ],
					"text" : "loadmess 120"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-42",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 264.0, 335.0, 62.0, 17.0 ],
					"text" : "prepend set"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-43",
					"maxclass" : "newobj",
					"numinlets" : 6,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 264.0, 315.0, 196.0, 17.0 ],
					"text" : "pak remix 1. 1. 1. 0. 0.1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-44",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 533.0, 234.0, 77.0, 17.0 ],
					"text" : "loadmess set 7"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-45",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 533.0, 270.0, 77.0, 17.0 ],
					"text" : "loadmess set 2"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-46",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 533.0, 252.0, 77.0, 17.0 ],
					"text" : "loadmess set 0"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-47",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 628.0, 289.0, 92.0, 17.0 ],
					"text" : "advanced features"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-48",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 514.0, 289.0, 112.0, 17.0 ],
					"text" : "windowing parameters"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-49",
					"items" : [ 3, ",", 3.5, ",", 4, ",", 4.5, ",", 5, ",", 6 ],
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 582.0, 269.0, 37.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-50",
					"items" : [ 0, ",", 1, ",", 2, ",", 3, ",", 4 ],
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 582.0, 251.0, 37.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-51",
					"items" : [ 512, ",", 750, ",", 1024, ",", 1250, ",", 1500, ",", 1750, ",", 2048, ",", 2250, ",", 2500, ",", 2750, ",", 3000, ",", 3500, ",", 4096, ",", 5000, ",", 6000, ",", 7000, ",", 8192 ],
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 582.0, 233.0, 37.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-52",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 510.0, 270.0, 69.0, 15.0 ],
					"text" : "oversamp $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-53",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 521.0, 252.0, 58.0, 15.0 ],
					"text" : "fftover $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-54",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 502.0, 234.0, 77.0, 15.0 ],
					"text" : "windowsize $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-55",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 420.0, 499.0, 17.0 ],
					"text" : "oversamp <num: oversamp> ... configure oversampling factor (STFT hop size = windowsize / oversamp) [4]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-56",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 407.0, 484.0, 17.0 ],
					"text" : "fftover <num: fftover> ... configure frequency oversampling (STFT size = windowsize * 2 ^ fftover) [0]"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-57",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 218.0, 394.0, 302.0, 17.0 ],
					"text" : "windowsize <num: windowsize> ... configure window size [1024]"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-58",
					"items" : [ "transpose", "off", ",", "transpose", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 221.0, 110.0, 72.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-59",
					"items" : [ "remix", "off", ",", "remix", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 238.0, 225.0, 56.0, 17.0 ],
					"textcolor" : [ 0.65098, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-60",
					"items" : [ "shapeinv", "off", ",", "shapeinv", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 630.0, 251.0, 67.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-61",
					"items" : [ "stereopres", "off", ",", "stereopres", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 630.0, 269.0, 77.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-62",
					"items" : [ "envtrans", "off", ",", "envtrans", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 225.0, 129.0, 68.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-63",
					"items" : [ "transients", "off", ",", "transients", "on" ],
					"labelclick" : 1,
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 630.0, 233.0, 76.0, 17.0 ],
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.713726, 0.0, 0.019608, 1.0 ],
					"id" : "obj-64",
					"linecount" : 5,
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 518.0, 316.0, 218.0, 58.0 ],
					"text" : "Note that the re-configuration of windowing parameters and processing features such as remixing and transient preservation will cause the re-initialisation of the SuperVP engine and may cause clicks when DSP is on.",
					"textcolor" : [ 0.713726, 0.0, 0.019608, 1.0 ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-65",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 449.0, 225.0, 66.0, 17.0 ],
					"text" : "loadmess 10"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-66",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 446.0, 333.0, 35.0, 17.0 ],
					"text" : "error"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-67",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 449.0, 294.0, 41.0, 17.0 ],
					"text" : "* 0.01"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-68",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 2,
					"outlettype" : [ "" ],
					"patching_rect" : [ 449.0, 225.0, 19.0, 86.0 ],
					"size" : 101.0
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-69",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 315.0, 167.0, 15.0 ],
					"text" : "remix 1. 1. 1. 0. 0.1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-70",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 207.0, 380.0, 123.0, 17.0 ],
					"text" : "attributes:"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-71",
					"maxclass" : "slider",
					"min" : -1200.0,
					"mult" : 10.0,
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 419.0, 129.0, 143.0, 15.0 ],
					"size" : 241.0
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"id" : "obj-72",
					"maxclass" : "flonum",
					"numinlets" : 1,
					"numoutlets" : 2,
					"outlettype" : [ "float", "bang" ],
					"patching_rect" : [ 371.0, 129.0, 46.0, 17.0 ],
					"triscale" : 0.9
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-73",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 300.0, 129.0, 69.0, 15.0 ],
					"text" : "envtrans $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-74",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 239.0, 275.0, 62.0, 17.0 ],
					"text" : "remixing of"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-75",
					"maxclass" : "slider",
					"min" : -1200.0,
					"mult" : 10.0,
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 419.0, 111.0, 143.0, 15.0 ],
					"size" : 241.0
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"id" : "obj-76",
					"maxclass" : "flonum",
					"numinlets" : 1,
					"numoutlets" : 2,
					"outlettype" : [ "float", "bang" ],
					"patching_rect" : [ 371.0, 111.0, 46.0, 17.0 ],
					"triscale" : 0.9
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-77",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 300.0, 111.0, 69.0, 15.0 ],
					"text" : "transpose $1"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-78",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 207.0, 367.0, 252.0, 17.0 ],
					"text" : "arguments: channels, windowsize, fftover, oversamp"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-79",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 564.0, 130.0, 201.0, 17.0 ],
					"text" : "spectral envelope transformation (in cent)"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-80",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 565.0, 111.0, 135.0, 17.0 ],
					"text" : "pitch transposition (in cent)"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-81",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 408.0, 333.0, 31.0, 17.0 ],
					"text" : "relax"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-82",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 371.0, 333.0, 31.0, 17.0 ],
					"text" : "trans"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-83",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 335.0, 333.0, 31.0, 17.0 ],
					"text" : "noise"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-84",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 2,
					"outlettype" : [ "" ],
					"patching_rect" : [ 412.0, 225.0, 19.0, 86.0 ],
					"size" : 101.0
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-85",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 375.0, 294.0, 41.0, 17.0 ],
					"text" : "* 0.01"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-86",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 2,
					"outlettype" : [ "" ],
					"patching_rect" : [ 375.0, 225.0, 19.0, 86.0 ],
					"size" : 101.0
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-87",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 338.0, 294.0, 41.0, 17.0 ],
					"text" : "* 0.01"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-88",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 2,
					"outlettype" : [ "" ],
					"patching_rect" : [ 338.0, 225.0, 19.0, 86.0 ],
					"size" : 101.0
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-89",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"patching_rect" : [ 301.0, 294.0, 41.0, 17.0 ],
					"text" : "* 0.01"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-90",
					"maxclass" : "slider",
					"numinlets" : 1,
					"numoutlets" : 1,
					"orientation" : 2,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 225.0, 19.0, 86.0 ],
					"size" : 101.0
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-91",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 285.0, 333.0, 51.0, 17.0 ],
					"text" : "sinusoids"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-92",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 301.0, 225.0, 72.0, 17.0 ],
					"text" : "loadmess 101"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 12.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-93",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 513.0, 211.0, 181.0, 20.0 ],
					"text" : "phase vocoder configuration"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-94",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 227.0, 301.0, 74.0, 17.0 ],
					"text" : "and transients"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.968627, 0.968627, 0.968627, 1.0 ],
					"border" : 1,
					"bordercolor" : [ 0.937255, 0.937255, 0.937255, 1.0 ],
					"id" : "obj-95",
					"maxclass" : "panel",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 515.0, 313.0, 224.0, 67.0 ],
					"rounded" : 5
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-98",
					"maxclass" : "gain~",
					"numinlets" : 2,
					"numoutlets" : 2,
					"orientation" : 2,
					"outlettype" : [ "signal", "int" ],
					"patching_rect" : [ 73.0, 487.0, 22.0, 78.0 ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-99",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 102.0, 58.0, 198.0, 17.0 ],
					"text" : "entirely based on SuperVP by Axel Roebel"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-100",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 102.0, 71.0, 191.0, 17.0 ],
					"text" : "Max/MSP integration by Norbert Schnell"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-101",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 151.0, 593.0, 27.0, 15.0 ],
					"text" : "stop"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-102",
					"maxclass" : "message",
					"numinlets" : 2,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 84.0, 593.0, 65.0, 15.0 ],
					"text" : "startwindow"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-103",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 0,
					"patching_rect" : [ 46.0, 592.0, 36.0, 17.0 ],
					"text" : "dac~"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-104",
					"maxclass" : "newobj",
					"numinlets" : 1,
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"patching_rect" : [ 117.0, 509.0, 60.0, 17.0 ],
					"text" : "loadmess 6"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"id" : "obj-105",
					"items" : [ 16, ",", 32, ",", 64, ",", 128, ",", 256, ",", 512, ",", 1024, ",", 2048, ",", 4096 ],
					"maxclass" : "umenu",
					"numinlets" : 1,
					"numoutlets" : 3,
					"outlettype" : [ "int", "", "" ],
					"patching_rect" : [ 117.0, 529.0, 72.0, 17.0 ],
					"types" : [  ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"hidden" : 1,
					"id" : "obj-106",
					"maxclass" : "newobj",
					"numinlets" : 2,
					"numoutlets" : 2,
					"outlettype" : [ "", "int" ],
					"patching_rect" : [ 117.0, 548.0, 70.0, 17.0 ],
					"text" : "adstatus iovs"
				}

			}
, 			{
				"box" : 				{
					"id" : "obj-107",
					"maxclass" : "gain~",
					"numinlets" : 2,
					"numoutlets" : 2,
					"orientation" : 2,
					"outlettype" : [ "signal", "int" ],
					"patching_rect" : [ 47.0, 487.0, 22.0, 78.0 ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 24.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-110",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 102.0, 7.0, 184.0, 34.0 ],
					"text" : "supervp.trans~"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 12.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-111",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 102.0, 39.0, 283.0, 20.0 ],
					"text" : "advanced phase vocoder transformer module"
				}

			}
, 			{
				"box" : 				{
					"data" : [ 6441, "png", "IBkSG0fBZn....PCIgDQRA....E....THX....fiQHeq....DLmPIQEBHf.B7g.YHB..X.dRDEDU3wY6blGlTUb0++SU0s2lt6YFXX1XF1WEWiHRTAEz3Fn3BRzW20DWhKIQMu3FFLf.AM95RLfQ+kXTbInFMZ70sDUTQQQi6P.zfxvLvvvruz8zceqpd+iaCLK8v1P.84Geedtyxc8Teum5bN0oN0U.XYuXmFN.vQN68vhw2QwacSH2SKCeWG6k.6lXuDX2D6k.6lXuDX2D6k.6lXuDX2D6k.6lXuDX2DNcuKWj921172eWD67il0A.wN.AXwh.CfCBoi2kYEcKgXOErBu1t0XPXcwBXQtcqJXYSZfd2osIEHvhxJvpBfHQiXKewXzI9tqxmEPHQU7nwDs2HcShUXwrCzfRqAtc8j.DXkJT5D39ueVBT0xnW.lcNwe2B1Z8Mj.0CzbyeC9GwEfafd.ZWDBaatptlc1rF319TEHrVLJIxTwwcUOCg13xXZ95CGtNDISe6Z68w1te1E20zOPqkzmqkL6Wy6DMBKRqH88qy20MaMAKBa5yXK6rcsQKB7ifUoRvszvZnpk8n3L7yBc3dgTmDKxshjuo6w1gSDKfvJvHk3n0X9xmC4F+L9INEwIpCSJigv6jNysXwhIs9sWSSh.DRvtEsGMlzMYuyxP6cdI27UK.g.wlumRvZwZ6HQj9EBVFMA4580G9kMVFwVwSgu887v0eDvjZKug2JXaRfBgDgMEhl2HxxeG52FWNSQUDmtIWpxlhOU4RU9ZEkhL+BS.VCXbAiFhHjL.7SPDD0JoWFIZfujDTHN7I9ZgZTFLofBvgAfex2HIKq.cGd.hzuDJWpItvxpIAMX0n7AJUaz.2D1bWDAFCjSR+bPV+Ld2r31b5KOTSUxpVwBQ0uwgNRIfz2l5driQf1zussJENIagT+qERt0tRtVmh4jX.fQvaJagavTAi7vbXA2WPJHOWrZ2NwhUrdKS8V07RuhkKldvoROvIcqJEVpAWdJZjOYeqia3x7wO7vgy+JRQsK0O2GkPHjo0TyjbBJfBMRTHn2H4NYirX+Mwseqv4LYIHZiEZqBKJDRHVRebkWugo8WZkoJKhyzzCFGgYoM0Ly4S9C7MgK.e66YiNbAfNEckANEvsJ52wr4iK1zoJcvIYSjZkOMQpcEbKNkxDz4hvZ38cRvT0UvnNJe7bOtOJozlwuLIABpa2ViMq4mciFdoWFtEJfIStsKHgESLtbQEL7oDm+e+V3HFqlE8tZt2G.tPcdreDLsL4cMwvP4jBAPvzlM7rZ5cbeH36SXd+jsxqrLWF6XrLnAB9CjdKnk.A032ulvYmhIdbNrpU6mGb4MPuUAYnVG5qM.6qSDVRh0RiMWNN8XvX8EEq0rY4XyuOVyq0diW1MKQNHS1LoV9BIZM+KtYm9vwnifAKuhSKbstkwgdTN7LOjf7xsYHlEz.taYSm.t24Cu3KJ3Fn.NVh1tGd4jheCUwIb7Vt6aC5aQfII7RuJjSRGFKga24+JzDmKkw4QYblrFlG0PxLnW5GA2BEfsBebm+Vnw5ZubgKdxZqP3fsxi8.FN+yIH2p6ZYAh5Ik.1OW+LKU+n3FJmTe9ifL1FPH8Qlh2ncDnzBVGIJsKtq5YIZ8eI2jSobBln3fkkphws3tdF3A3iE968QuJLNzZmtmdM32.9c+Q3zIGNwNPd.77zHN4q4l94Pu5o29VWkvR+H3vIK5IpMetUiK2CUyALdWVv8CWzkX34hTGyfMzI6h.TL93rHWd9WFd5+VlkO.HI32eq7f+ORN4IjE2soJdAYiHDBFkID2npT5YyqC2U9jnRTGBouN0QNMAlNv.kBmVqmTKeAja0eAyP0GNAcTzVMujLFS0ccL9iM.+8+hOJrjlfXYVtRkBdzmBhVuONW5QmNdcn4uSSbxGObnG7V1eEqGV65f8McW2MgOf3jJKMWykCm8jgeyuB9UWOrjfMy7nlLJCmDYyHbCxS8bPysrUHwDf+.MyS9vvkcwgYltUxCKqm3XXr5Pb6N8iRanBR8EOJxVVGVku1EFkD7BSQHcP51BoV9elv0rBtEm9vQah.BCuppYtdc4bviygm7gETTwM0kZd.7O+T3MVLLExk7ZilzlvaSKDKhKm1DaejBUWqWisfN3a6KnUF5ffCbe89ee9fe5k.+7KG9yh54Mn4N8L7ifyfb4c+.3seusBABPJHn+VXd2slK37CwcoWOOgnILRIiRmEyzoOzqlJiTK6IPEqZDJ+aN1RI.Vo.Ux5wrh+B42v2vzbJgiRGgjn4MkwYNtUwwdrg3wueEYGsEH9VWdd0EAwqRwX5fcrMgOl3L3A.eu8u86OYRvpg.soihFKqjDLn9C8rMJyJEbMWNbziEdPpkVxf8oCkPDpIGdo+w1f.AHAHMw42NWCWxOJByyTIOunARHrbP5fbap9R+iUCtqXgnZY8fvSwPBfTmByxWHAqdYbKN8iIpyAPyKJZlo5VNi+T7yy+3RJtjlwtUz7.HVbXQuCLbBPgYHJIMVJijLrACEle6OlTBH7rwuk1kkJIEEWHDve6O+7yCtn+Knbmj7Oyva0rQw9SPVxGB0U+1f.E.IkjUVw3AtOWtfKLJyPWIOurARhkwnCyM6q2DrguA8JeFjlDag.MJGD4MBRIc3KswHovyksT5EpZdYCACogT.1s9nNprJX0qoy1wZKgTEtTRwoIr1.e97FDRp13Xv.jDKNcQH+i6H75Z+OnoLd7QPPVyZ8rstsfUXAqD2DJpoQC4hhhs9PZglUFVrtYR5D.YwGBVoOf13E112whYfGOyWuQdDQsnQxDLYy+spXdnEDiYMG+PffayTvVw5gZpEFFAx3waFCwvRA8pyGymCnjzovSjH5xADTRwdj3RIN0zNcWOzW7Qs0CqYsac4F.gikjoBvkbUJ9aOSKLCUuYLlPnkv7Yi7H15gAexPwiFicSxF.XwZrHKYL3VzAw8qqh2R0HJqkS0FkIIxkoM2lXgOYHDcNhj1g5a.RjD5C9x3wigAMVhFoyGywwaysMDnCBhfjjo55m4Ae.PyRM+aRzoiUH9PqgMrwt95sj1mPH+ba2gC+oGsQtBUAbDDBPwKIajGQWKhdOZn3Qg1jBDswKLVAVqFCRTCZRjLuQvc3VAqRkD+FEWspWLJ2r3RttF4Cd6vP3tVMLQRPqgr5BUUSZAV0YmyHkd6uscgU.Ygf3w65gkNn9CQi.UPmY4nokisZnL.hnJdrEDjYdWsvYI5EmkMaPCKV0D2i65gB+dHF3IfwXQX2R5LZ+HQrZr9ifugdZrgrJj4oqf5czjuqjo6q2zyZ8y4esIoxxiPWXhCkzKzjtLEVo+sty81HXfLQfBBijlagtTKrf7gbyApNCcg273t2JZvhvRVxhxlK+Fhw3SEkeprWDxnX4NIY1tkS84zWTCcRXTAQXcosiKtcDn.KncQGHWbF5jYQJEywrNpWInzTJlkpH1vmY3TN2TTSUQ.+cTTfPg.GkWvxYBAQfCBRjrqu11RfR7zliEGRz4dn.PjvdaMmgm4lLG3KCVTr.jkjO+iCyobQMSo03mopJjPZKk6jh4nKiJB1CbF5jwnBivn6Hk0w9YdLq0lDSOGDpAb77xll3gEa.izx9YBxzcJlktzDboWmEKYAp1qskWO7HhugLvP.QPgODrwp67w7k1FXGUVBijV6BxC7z586CZMC58MjlTyjMWQPIMTaTNyeRRTqSxLcJlh0JRnfeioBVluP3eD+WXCWhW93x.xngJgUf0sUDEOJj8c77Xt0wyqpGMVFqNKllrHdl+ZLl6c6.gB1tQSTTAdis8KyfAcvKH47PQkUkAhP4QhI6PPwYgjVaMyc6AOaiValaLUhK9864stcvmf5aHBWzUmhx9LCyTUBCV6PbokGfMxhvE0fOIbycfXLYVY.1FAkXDRDC73HYgeOt8TUxanZFkUvjIG9whB4FmYSLu4GDhDXylEJHeXnCBVUWPf9QPI3iUuFnwND5lR40UKSZfwaktTKrkXPSMCQyvvFWEIHud.Cnus8AAnBvUbcFd1+ZbtImBYTlffPviKqk+ntVb52QCENRvM4VcNi5RBTjNUxVqBmANAhkce3tcqfUpRhxX47k4xQ3Fgq5lahW9EB.Q8tUA7Ci7.guhjTaWXGb.3mUuFnrx67w74iNkgkrRSfw6hQAstJ8FGcQYXjOKmVYXCtMZfBfvNb22mOdhE1B+XY9bLlv.FdSYL9CtUAEdvX6yQh0z4DD2QrsmLCqKl.4hyHNWJOZILCSY70NZhpMLMmhXTwCw4bkw3SVZDHrmFvQOVP0CM+ytHcMGAgYcqCdmk198GJnmsr3cnKb.DjHQW2E989mPKMJXDcHzf0QJ9bZkwcDdNYP.DxGO5CGkqa5w3hE8hKUjGArBdOULls6ZIV9G.NC8zwJUdCLeafsuYCxjBSV4gZnmFKS4vbLqkFTPwtJltSuIqMn3LujT7MeUXHjmF3HOP3+klx36uQP.FnI.u7qS67F62u2VhNbUARGHsaFriWSsvK9O7F4yP5PXAuMsPNEa3D+Ao0ihn3E9aYw4c0MxgmLLWrLO7osrZGWlsaYTU3BvYvmDFkeDFCaOS361+zo4lDxtOnFxox6Yc4dXCDSBE6JYlpdSEqHEm1EljMtgHDMOXhGKrThwaQmif0AAiiH7luK7gexV1+l7llIMvToHig97pKBdy2ENCxAUaZvUhKON0y3GiWVeDgk7YePHtveVKL7DA3FTEPXskZUvbbKmxBlKNCaJXBzCv3tM53tErCMejVsFJZj3LnIxypaf+prNjRIGnM.+RUw7oeTRtloYwRHNsIAG9nfml5y3DuebDAa8Jl+C4kAGvajHABjIMPAIS44nns3esJ3+Y9P+SFfwQ6iS4YoAr8zky+LE3KJrw0kMWz0XIvFTLamRoTsCwkBtOpjOTZwYXSFatCvadE1AvN3D5ZwZRgszi.JcLLO2MvyKqGLvwZBysp5MO9SDmq3m6iRJIHW0kBKKPbleFxZbuwGWN4wS9bvcNOu843j4347mVCrsilXsU.Wyzf09INbyTP6xg3aRKrPpmy6LgwOdK0VcXNkKnUV0GlfYq5M8yURBog4QU7r5FPMnSBaOGNZ2jriVmJ63Umk0hFv2.NAZNVsL2ZWN4q7wgYBxIaiRkx7Y9+wMPeJMJW20YXIKMI+tGrNFAAX7cPKYRjMecpjLm6tdRlDtnyFxJDTcGHvfHw0ELoUk+6KBl0cAuy6JXVjOCoMY94qHI2NUwXFqkewOUfi+fbYWGrj2II2gpDN.S.TBAOkrQdT2ZQ1uiEJYzn0ZjaiJoHSncSq41e8YYwp7gSN8gDMTFqHQELZUNjqVvvkAoQqk6eoMyHGQVb1mulu7eY3A+pVnGnX3cvS4nHDFWAy68akOYYdgjjpJESjr2r7rQb4UTMQgE.u7qASetP7U4moSgbXsIq2KgXLMVO8c+zbu2NLrg6mY7qCv7dfXbCphXB1rQB79pVYttkShhNTTCZBXvhbmn5xrq401YHPABrHLVrAxFmbFHUW8xXEt0v2WkK8TCGhJBqHYBt+E0BSbbQ3zlbJ97uvxitlXDCCGHg17f7kH36QHFlMHKtrT74U4RTjLIxdyNEpEMOuoQVxGBe066vDhmC2HEx.ZiW2mmFYNTEC8fL76lKbniygG3Axhq6lalKVlGWD8.GikOyII2r62P04NH7sOmAZkODaipOXqQfdkdyXm8N.A1An7iZieFoV9Svwa8yuTTJYYfxTZtZc4HGhkE+b9IgtIl0bfG+u.EmvO+PxkiiHDpMlg0XYMjhpwkQRnMSf0fKKlXzO7wvHP6tlURB9CTKKlV3DNZ3NtUX+Fkf2awQ3Gblw4PpML+JUQDUCU43xT0qgOOP13reW.5nEgnqBtb6.l25l1Yz.2Br3kaLajBQEnG7k0sLbEI3fkQnGFEGnJKdgpah25KLbomueN4I3x9LX3iKSySugV3MoEbwRXjDEINHnGnnD70tJXHKjLbBPQ3iMMyrqiT7jz.ykMRc8HIS8pEL6o4v.FtgU+U4vIctsR+VW.lspXxVC06HXVl0wRCDBei3rwDsTDZMVgcKElzNZ6emqKbafvldHeVDY6MXyOq1ufnJer+DfBsJFjSPl2WWG0VWPNoI3v9O7TbbGkj92WKUlPyKVWLd5jMxqSy74zJqljzJVJksLI1Mgg2iX7EzJKhVXATG+dpkUlcLNgiyxu9Vfy8r7S3bbnt5BwTtXMU7YZtCUITh1gVUBtaVOuLwwYXSAadCGq1EgXSkMxNWEh1sIvMUqHdkinEYN8Aahl3iabUTpJ.C1Fj9X8Q1JGt2OpVZMQ.N1iUR3fJNrCQyoOQXziDxOenEeZVoNIuSqwYUlDsyF3pIIWCqi2VzBeU33DZfo3DNdK+xeA7ytLX3CAT90jzUw4boBdyWON2tpDNHc.LBI+IQ0r.ccHGvIhn3C0i7PuSSbsk.6lEYtG7bpnwJcvYPSfVasdtm5+RFjuPL7T9XJ1b3eKRvseO0ygbPQYJmUSXaxR1QgS7X71hE2K4BW6s.q70x7y4p9Qv4NEnukBEzq1LqdBfn94dlkedg+2l4+VUBGgIKrBAutpQ9itUCkNFD88HwX27jJvth55dWzxbPfUH.qKZmvnFwTXCQKkYkpL9ZmTnLVtVQAbblb3GcsMwhe8HHB09GcVg7zj5cQcwSP3osdnGrWNGa2ThFxgG6gBwzuiXbVhdxjsQQZE7lplYVtUPh7O.jC53QaoMIHXWSQwuKdchH.aJzA5INCex7Y9Cvz0qk5TPDC7yT8hbqwOS4xZk07ui.g5P4hY25IMMSISfnBVzajEm6OsIFYyg4pj8hfVCqzoUlq6Zo9r6CpgdJfvOxNLeF6Jv+QVnMRSBLQ5CxAOQ9TAb+1JItD5sVwzTERKkY3R9EtzRKg.mtgtPPAq6axhe70Em8IdPlpp.hnETizvcoWOUFt.OmF9hh0rohj7a8DnLcgMmDQgiB0POEdZaC7XxZPKgQaBwrU8g+9q1J2vL.BE.wNiTD.pq1HbFWLTyJgYpJl9qUzjxvsSk7dJKNC6GhMRIfIkW8RuCOPssqV6tZrkEsiUmBQQiBYIigGzci7bxFvfkC23mqT1KtueeBtm6JDDz2NlhgBLl.bkS0vRd2XbSphXPF+jTJ3AoZdEcSnFvDvja+8VGK6h05ZK9O7Zkyh1ZPLvSjjEbPbWtqm2VEGIBNO5ImtaN7ymVC7nOVHHRFlo8tRhCH4lmohmXgI3lk8liwjEVA7jh5XAlZQLfiAJYzfqKhcwZbYRb9OJDVCVgOTC8jo4bG.2oa4rRmjDzX4pb5EGlNB+jaJNe5GFDxvTO1ID1g+zijE+56JNWlLONCQNHAdcUKLe8Fv16Qir+GGVCvtfX81VX2yp0zlBiuHnF1Tnrr5ESOUY70JC8TKXFNEy.qN.m34jjO9CBu0SvVPI+4GOK9QWSLljMWtPQtf1vRUIX1oJiV54vvY.SLM4s8kR9tK1MsbWEHzFHqBvYvShUpTbu10SbAjuVwM5T.srFAWz0nIYhtlAW2ZCvkc8wX3sFjqVkOg0Rpzwk6xsbpMbA3LjIgwIz10jAsqB61VuvVrX0IwzygibelBuAI39oJRJLLLW+LaUw70ejKK4C5Zsl48mRQgaTwrUkPdZAU3jhYnqfUDHBxQbVXBUf2JLZ2H18ufqMoPj+AibfSfEXpiGQVKFofwZCyzbJlu9axbITHDP0kYYFNkP+MJhqf6zrdVhHINC8zgH8a2N4A6QVw5Vz5TH68ghsvQxC5tQdMYSXrZNZaXN5tn.DsVAWgp.Fta.RIr7X1p4ePbTCbBXyanX0oXOwZVdOxR9WfFsvA0PlDIxae31cKmk4jD+F3VkExHEY0oq4RU4w4XyADBdEUi7.5ZP1ue.zmiBcZmF6IV1x6gHPKX0XbBgyPNUpIbwLS2J3qTIIpQxUJJncjQOEJtHxCoEdaUybmoVOt4ueHK8HwZzHQyVVbs6dwdnO5Doyjn1EandhugOYVoufba5xYiRKYancSRdA3C+FKKWkjes6Zotb6GNC8zvH8k1i6dtkL+d1uZGBug64lSew2PNM9XofeOURRkncK2YkEpxQycpqf0kUd3anmIFeY6s9Y2CX2qsXOJAZEFu4Uw0ESA6Gpgbx7LllX9TMBoC9QhOjzrxxzcqfOxQgyv9gnCW.VcRrhcOAKu0vdTBTX2xZM2XbQVzgB896yBz0vqHqm.BIBoh+fsZVhsUbF3IhM2AhUmDgPjNCK6YwtjT528g27LqERTC7Dw0ME28F9PJzWPVssUVntQTCdhXK9P7xvi0tcsb72cfukPfhze1UzXjAwYHmJ0krdtg59Ju5bsziDQoiGiIEVLH9VB4A6ochzA38A2vEiS.bF7IQ89ykD8XnH6+3vXcYOUrdaM7sDMvN.cJLg6MN6+4C9ifQEd2ZBB1QfGAtoueK6AEj1AAfIE5nkl9S9gdOsy1tDs6Cuy2tfXKqMiukRdv2xrA9cQrWBrah8RfcSrWBrah8RfcSrWBrahc805v+eF9+TunF7l1RvN9.....jTQNQjqBAlf" ],
					"embed" : 1,
					"id" : "obj-112",
					"maxclass" : "fpic",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 16.0, 16.0, 80.0, 80.0 ]
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-113",
					"linecount" : 2,
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 115.0, 499.0, 102.0, 27.0 ],
					"text" : "keep i/o vector size close to hop size"
				}

			}
, 			{
				"box" : 				{
					"fontname" : "Arial",
					"fontsize" : 9.0,
					"frgb" : [ 0.0, 0.0, 0.0, 1.0 ],
					"id" : "obj-114",
					"maxclass" : "comment",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 102.0, 84.0, 126.0, 17.0 ],
					"text" : "IRCAM - Centre Pompidou"
				}

			}
, 			{
				"box" : 				{
					"bgcolor" : [ 0.74902, 0.823529, 1.0, 1.0 ],
					"border" : 1,
					"bordercolor" : [ 0.0, 0.333333, 0.909804, 1.0 ],
					"id" : "obj-115",
					"maxclass" : "panel",
					"numinlets" : 1,
					"numoutlets" : 0,
					"patching_rect" : [ 66.0, 585.0, 120.0, 30.0 ],
					"rounded" : 12
				}

			}
 ],
		"lines" : [ 			{
				"patchline" : 				{
					"destination" : [ "obj-76", 0 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-116", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-118", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-116", 0 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-117", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-3", 0 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-37", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-2", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-1", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-21", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-22", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-59", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-36", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-73", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-77", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-58", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-62", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-54", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-53", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-52", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-63", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-60", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-61", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-25", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-32", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-30", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-37", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-43", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-60", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-5", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-63", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-5", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-49", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-45", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-50", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-46", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-51", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-44", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-53", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-50", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-52", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-49", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-54", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-51", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-43", 5 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-67", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-67", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-68", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-68", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-65", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-31", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-35", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-35", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-23", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-71", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-40", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-75", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-41", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-43", 4 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-84", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-29", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-31", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-43", 3 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-85", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-85", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-86", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-86", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-92", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-26", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-24", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-33", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-20", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-72", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-71", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-76", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-75", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-43", 2 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-87", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-87", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-88", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-88", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-92", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-69", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-42", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-43", 1 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-89", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-89", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-90", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-90", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-92", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-25", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-26", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-32", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-33", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-30", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-29", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-73", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-72", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-77", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-76", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-42", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-43", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-59", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-5", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-62", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-5", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-58", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-5", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-106", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-105", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-105", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-104", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-103", 1 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-98", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-98", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-107", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-98", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-3", 1 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-103", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-102", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-103", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-101", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-103", 0 ],
					"hidden" : 0,
					"midpoints" : [  ],
					"source" : [ "obj-107", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-107", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-4", 0 ]
				}

			}
, 			{
				"patchline" : 				{
					"destination" : [ "obj-107", 0 ],
					"hidden" : 1,
					"midpoints" : [  ],
					"source" : [ "obj-3", 0 ]
				}

			}
 ]
	}

}
