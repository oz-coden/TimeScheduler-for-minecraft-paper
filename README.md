# TimeScheduler for Minecraft Paper

Japanese section is [available](#目次) in below this section.

## Table of Contents

1. [Summary](#summary)
2. [Background of Development](#background-of-development)
3. [Features](#features)
4. [Usage](#usage)
5. [Dependencies](#dependencies)
6. [Installation](#installation)
7. [Setting](#setting)
8. [LICENSE](#license)

## Summary

This offers a time announcement feature that notifies you when it is exactly X o'clock in the real world, as well as a feature that allows you to set reminders based on real-world or in-game time on the Minecraft Paper server.

## Background of Development

Since Minecraft doesn't have a feature to display the real time, we often lose track of time while playing the game.  
This feature is designed to solve that issue.

## Features

- Time Annoucement  
This plugin broadcast time announcements at round-numbered times.  
(This feature can be turned on or off.)  
- Reminder
You can set reminders by specifying real-world time or in-game time.  
- Auto Detect Language And Switch Message  
Plugin detects the language settings of the player's Minecraft client and displays the appropriate message in English (en_us) or Japanese (ja_jp).  
You can also register other languages by providing translation files if you need.  

## Usage

You can use commands in below:  
|Command|Description|Example|
|---|---|---|
|/schedule get (&lt;target&gt; &lt;schedule-type&gt;)|Check the schedule. To add arguments to filter.|/schedule get|
|/schedule set &lt;target&gt; &lt;schedule-type&gt; &lt;time&gt; &lt;message&gt;|Set the schedule. |/schedule set example-player real 1d This is tomorrow reminder!|
|/schedule remove &lt;id&gt;|Delete the configured schedule by specifying its ID. (You can identify the ID using "/schedule get".)|/schedule remove abbdf838-1066-07b8-bbc9-3dd5fa6362d3|
|/timesignal &lt;true\|false\|toggle&gt;|Set time annoucements is enable or disable.|/timesignal toggle|

## Dependencies

- Minecraft Paper Server 1.20 or newer  

## Installation

1. Download the latest `TimeScheduler.jar` from the GitHub Releases page.  
2. Place it in the server's `plugins` folder.  
3. When you start (or restart) the server, a `config.yml` file and a `lang/` folder will be automatically created inside the folder named after the plugin.  

## Setting

You can change the basic settings in `plugins/TimeScheduler/config.yml`.  
Also you can customize all messages by editing the `.yml` files in the `lang/` folder.  
If you want to add a language, place a translated `.yml` file with the corresponding language code name in the `lang/` folder.

## LICENSE

This project is released under the MIT License.  

---

# TimeScheduler for Minecraft Paper

## 目次

1. [概要](#概要)
2. [開発背景](#開発背景)
3. [機能](#機能)
4. [使い方](#使い方)
5. [前提・依存](#前提依存)
6. [導入方法](#導入方法)
7. [設定](#設定)
8. [ライセンス](#ライセンス)

## 概要

Minecraft Paper サーバー内で、現実世界でちょうどX時になったときにお知らせしてくれる時報機能と、現実時間やゲーム内時間に準じたリマインダーを実行できる機能を提供します。

## 開発背景

Minecraftには時刻を表示する機能がないので、しばしば我々は時間を忘れてゲームをしてしまうことがあります。  
それを解消するための機能です。

## 機能

- 時報  
X時ちょうどのときに時報を放送します。  
（この機能はオン／オフを切り替えることができます。）  
- リマインダー  
現実世界での時間またはゲーム内時間を指定して、リマインダーを設定できます。  
- 言語の自動検出とメッセージの切り替え  
プレイヤーのMinecraftクライアントの言語設定を検出し、英語（en_us）または日本語（ja_jp）で適切なメッセージを表示します。  
必要に応じて、翻訳ファイルを提供することで他の言語を登録することも可能です。  

## 使い方

次のコマンドを使用できます：  
|コマンド|説明|例|
|---|---|---|
|/schedule get (&lt;対象&gt; &lt;スケジュール種別&gt;)|スケジュールを確認します。フィルタリング用の引数を追加できます。|/schedule get|
|/schedule set &lt;対象&gt; &lt;スケジュール種別&gt; &lt;時間&gt; &lt;メッセージ&gt;|スケジュールを設定します。 |/schedule set example-player real 1d これは明日のリマインダーです！|
|/schedule remove &lt;ID&gt;|IDを指定して、設定済みのスケジュールを削除します。（IDは「/schedule get」で確認できます）。|/schedule remove abbdf838-1066-07b8-bbc9-3dd5fa6362d3|
|/timesignal &lt;true\|false\|toggle&gt;|時刻アナウンスのオン／オフを設定します。|/timesignal toggle|

## 前提・依存

- Minecraft Paper Server 1.20 以降

## 導入方法

1. GitHubの Releases ページから最新の `TimeScheduler.jar` をダウンロードします。  
2. サーバーの `plugins` フォルダに配置します。
3. サーバーを起動（または再起動）すると、自動的にプラグインの名前のフォルダの中に `config.yml` と `lang/` フォルダが生成されます。

## 設定

基本設定は `plugins/TimeScheduler/config.yml` で変更できます。  
また、`lang/` フォルダ内の `.yml` ファイルを編集することで、メッセージを変更できます。  
言語を追加したい場合は、対応する言語コード名をつけた翻訳済みの `.yml` ファイルを `lang/` フォルダに配置してください。

## ライセンス

This project is released under the MIT License.  
