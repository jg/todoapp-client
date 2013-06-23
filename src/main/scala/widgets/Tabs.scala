package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import android.widget.TabHost.TabContentFactory
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import android.os.IBinder
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.widget.AbsListView
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._
import android.database.sqlite.SQLiteDatabase

abstract class Tab
case class incompleteTasksTab extends Tab {def apply ={}; override def toString = "incomplete"}
case class completedTasksTab extends Tab {def apply = {}; override def toString = "completed"}

class Tabs(view: View, listener: (Tab) => Any)(implicit context: Context) {
  val tabs = List[Tab](incompleteTasksTab(), completedTasksTab())
  val tabHost = view.findViewById(android.R.id.tabhost).asInstanceOf[TabHost]

  tabHost.setup();
  tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
  tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
    def onTabChanged(tabId: String) = listener(tabs.find(_.toString == tabId).get)
  })
  tabs.foreach((tab: Tab) => setupTab(tabHost, new TextView(context), tab.toString))

  def createTabView(context: Context, text: String): View = {
    val view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
    val tv =  view.findViewById(R.id.tabsText).asInstanceOf[TextView]
    tv.setText(text)
    view
  }

  def setupTab(tabHost: TabHost, view: View, tag: String) {
    val tabview = createTabView(tabHost.getContext(), tag)
    val setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
        def createTabContent(tag: String): View = view
    })
    tabHost.addTab(setContent)
  }

}
