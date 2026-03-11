# SentryCore Wiki

Welcome to the **SentryCore** Wiki! 
SentryCore is a highly interactive, tower-defense style plugin for Minecraft (Paper 1.21.1) that allows players to craft, deploy, customize, and upgrade powerful automatic Sentry Cores to defend their bases from hostile mobs.

---

## 🛠️ Getting Started

### 1. The Core Components
Before you can build a Sentry, you need two custom items:
1. **Sentry Core**: The heart of your defense system. It is represented by a Conduit block.
   - **Crafting**: Place 4 Netherite Ingots, 4 Amethyst Shards, and 1 End Crystal in a Crafting Table.
2. **Condensed Obsidian**: The currency used to upgrade your Sentry Cores.
   - **Crafting**: Surround 1 Diamond Block with 8 Obsidian blocks in a Crafting Table. It appears as an enchanted piece of Obsidian.

### 2. Building the Structure
A Sentry Core cannot simply be placed on the ground; it requires a specific, charged structure to function.
1. **The Base**: Place a **Barrel**. This will act as the Sentry's fuel storage.
2. **The Frame**: Surround the Barrel on all 8 sides (same Y-level) with **Obsidian**.
3. **The Pedestal**: Place exactly one block of **Obsidian** directly on top of the Barrel.
4. **The Core**: Finally, place your crafted **Sentry Core** (Conduit) on top of the Pedestal. 

*If built incorrectly, the Core will refuse to place and will drop back into your inventory.*

---

## ⚙️ Operating Your Sentry

Once placed, sneak (`Shift`) and `Right-Click` the Sentry Core to open the **Main Control GUI**.

### The Main GUI
- **Power Toggle (Slot 10)**: Turn the Sentry ON (Lime Dye) or OFF (Gray Dye).
- **Mode Selector (Slot 13)**: Change the firing mode and required fuel type.
- **Access Fuel (Slot 16)**: Remotely open the Barrel beneath the Sentry to insert fuel.
- **Upgrades (Slot 22)**: Open the Upgrade Menu to spend Condensed Obsidian on permanent stat boosts.
- **Target List (Slot 24)**: Open the Target Selector to whitelist/blacklist specific hostile mobs.
- **Pick Up Sentry (Slot 31)**: Safely dismantle the Sentry Core, retaining all of its upgrades, targets, and data as a physical item in your inventory.

---

## 🔫 Firing Modes & Fuel

Sentries require fuel to fire. Fuel is consumed directly from the Barrel beneath the Core. There are three distinct firing modes, each with unique effects and fuel requirements:

### 1. Amethyst Mode (Basic)
- **Fuel**: Amethyst Shard
- **Effect**: Fires a fast, glowing projectile that deals standard physical damage.
- **Base Fire Rate**: Every 20 ticks (1 second).

### 2. Prismarine Mode (Laser)
- **Fuel**: Prismarine Shard
- **Effect**: Emits a continuous, tracking Guardian laser beam that damages targets continuously over 1 second.
- **Base Fire Rate**: Every 20 ticks (1 second).

### 3. Echo Mode (Sonic Boom)
- **Fuel**: Echo Shard
- **Effect**: Unleashes a devastating Warden sonic boom that bypasses armor and deals massive burst damage at long range. 
- **Base Fire Rate**: Every 40 ticks (2 seconds).

---

## ⭐ Upgrade System

Sentry Cores can be permanently upgraded up to a maximum total of **40 Tiers** (Configurable). Upgrading requires **Condensed Obsidian** inside the player's inventory.
All Upgrades are accessed sequentially through the Upgrade GUI (Slot 22).

| Upgrade Stat | Description |
| :--- | :--- |
| **Range (Bow)** | Increases the block radius in which the Sentry can detect and lock onto targets. |
| **Recharge (Clock)** | Reduces the cooldown (tick interval) between each shot by a percentage. |
| **Damage (Sword)** | Increases the raw damage output of the Sentry by a percentage. |
| **Emergency Healing (Golden Apple)** | A reactive safety net. Heals the owner if their health drops below a specific threshold while near the Sentry. |
| **Multi-Target (Crossbow)** | Allows the Sentry to lock onto and fire at multiple completely different enemies simultaneously per shot cycle. |

### Emergency Healing Mechanics
The **Emergency Healing** upgrade replaces passive buffs with a highly reactive safety net. If an owner takes damage while within 15 blocks of their Sentry, the Core will intervene *before* the damage is finalized:
- **Tiers 1-9**: Triggers when health drops below specific thresholds (e.g., 2❤ at Tier 1, up to 6❤ at Tier 9), granting potent Regeneration, Resistance, and Absorption buffs on a cooldown.
- **Tier 10 (Resurrection)**: If the owner suffers **Fatal Damage (0 HP)**, the Sentry intercepts the blow, sets health to 1, triggers a Totem of Undying effect, and grants monumental buffs (*Inst. Health, Regen, Res V, Absorption IV*) on a 10-minute global cooldown.

---

## 🎯 Target Management

By default, freshly crafted Sentry Cores will target *all* hostile Minecraft mobs (Zombies, Skeletons, Creepers, Phantoms, Slimes, etc.). 
However, players can fine-tune exactly what their Sentry attacks by accessing the **Target List** (Zombie Head) in the Main GUI.
- **Green Display Name**: The Sentry **WILL** attack this mob type.
- **Red Display Name**: The Sentry **WILL IGNORE** this mob type.
*Clicking any spawn egg in the menu will toggle its tracking status.*

---

## 💾 Persistence & Portability

SentryCore features highly robust data persistence.
- **Block-Bound**: If the server restarts, crashes, or unloads, the Sentry Core securely retains all its Tiers, Target mappings, and modes natively inside the Conduit block's PersistentDataContainer.
- **Item-Bound**: If you pick up the Sentry Core via the Main GUI (or break the Obsidian frame), the Core physically drops as an item. **All upgrades and settings are embedded into the item itself.** You can trade it, store it in a chest, or deploy it elsewhere, and it will immediately resume exactly where you left off!

*(Note: Direct breaking of the Conduit block is magically prevented to protect your investment. You must use the GUI or dismantle the frame to move it).*
