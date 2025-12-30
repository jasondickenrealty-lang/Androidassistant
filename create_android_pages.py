import os

# List your page names here
pages = [
    "Home",
    "PropertyList",
    "PropertyDetail",
    "Chat",
    "Profile",
    "Settings"
]

base_dir = "app/src/main/java/com/example/androidassistant"
layout_dir = "app/src/main/res/layout"

os.makedirs(base_dir, exist_ok=True)
os.makedirs(layout_dir, exist_ok=True)

activity_template = '''package com.example.androidassistant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class {name}Activity : AppCompatActivity() {{
    override fun onCreate(savedInstanceState: Bundle?) {{
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_{layout})
    }}
}}
'''

layout_template = '''<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".{name}Activity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="{name} Page" />

</androidx.constraintlayout.widget.ConstraintLayout>
'''

for page in pages:
    activity_name = f"{page}Activity.kt"
    layout_name = f"activity_{page.lower()}.xml"
    # Write Activity file
    with open(os.path.join(base_dir, activity_name), "w") as f:
        f.write(activity_template.format(name=page, layout=page.lower()))
    # Write Layout file
    with open(os.path.join(layout_dir, layout_name), "w") as f:
        f.write(layout_template.format(name=page))

print("Placeholder Activities and layouts created.")
