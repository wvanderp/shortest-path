# Pathfinding JSON Test Format

This document describes the JSON format used for pathfinding tests. Each JSON file contains a single test case that specifies player setup, the path to test, and the expected results.

## File Location

Place JSON test files in: `src/test/resources/pathfinding-tests/`

All `.json` files in this directory will be automatically discovered and run as unit tests.

## JSON Schema

```json
{
  "name": "string (required) - Descriptive name for the test",
  "description": "string (optional) - Detailed explanation of what the test verifies",
  "setup": { ... },
  "test": { ... },
  "expected": { ... }
}
```

## Setup Section

The setup section configures the player state and pathfinder settings.

```json
"setup": {
  "inventory": {
    "itemId": quantity,
    "772": 1
  },
  "equipment": {
    "itemId": quantity
  },
  "skills": {
    "AGILITY": 99,
    "MAGIC": 75
  },
  "quests": {
    "QUEST_NAME": "FINISHED"
  },
  "varbits": {
    "varbitId": value,
    "6571": 100
  },
  "pathfinder": {
    "avoidWilderness": true,
    "useAgilityShortcuts": false,
    "useGrappleShortcuts": false,
    "useBoats": false,
    "useCanoes": false,
    "useCharterShips": false,
    "useShips": false,
    "useFairyRings": false,
    "useGnomeGliders": false,
    "useHotAirBalloons": false,
    "useMagicCarpets": false,
    "useMagicMushtrees": false,
    "useMinecarts": false,
    "useQuetzals": false,
    "useSpiritTrees": false,
    "useTeleportationBoxes": false,
    "useTeleportationLevers": false,
    "useTeleportationPortals": false,
    "useTeleportationPortalsPoh": false,
    "useTeleportationSpells": false,
    "useTeleportationMinigames": false,
    "useWildernessObelisks": false,
    "useSeasonalTransports": false,
    "useTeleportationItems": "NONE",
    "currencyThreshold": 10000000,
    "calculationCutoff": 30
  }
}
```

### Inventory and Equipment

Specify items by their numeric item ID and quantity. For example:
- `772` = Dramen staff
- `563` = Law rune
- `556` = Air rune
- `554` = Fire rune
- `995` = Coins

### Skills

Specify skill levels using uppercase skill names:
- `AGILITY`, `MAGIC`, `STRENGTH`, `RANGED`, `MINING`, `WOODCUTTING`, etc.

Unspecified skills default to level 99.

### Quest States

Valid quest states are: `NOT_STARTED`, `IN_PROGRESS`, `FINISHED`

By default, all quests are treated as `FINISHED`.

### Teleportation Items Setting

Valid values for `useTeleportationItems`:
- `NONE` - Don't use any teleportation items
- `INVENTORY` - Use teleportation items from inventory
- `INVENTORY_NON_CONSUMABLE` - Use only permanent (non-consumable) teleportation items from inventory
- `INVENTORY_AND_BANK` - Use teleportation items from inventory and bank
- `INVENTORY_AND_BANK_NON_CONSUMABLE` - Use only permanent items from inventory and bank
- `UNLOCKED` - Use unlocked teleportation items
- `UNLOCKED_NON_CONSUMABLE` - Use only permanent unlocked items
- `ALL` - Use all teleportation items
- `ALL_NON_CONSUMABLE` - Use all permanent teleportation items

## Test Section

Specifies the start and end locations for the path. Locations are specified as `[x, y, plane]` arrays.

```json
"test": {
  "start": [3222, 3218, 0],
  "end": [3225, 3218, 0]
}
```

Coordinates use the RuneScape world coordinate system:
- `x` and `y` are world coordinates
- `plane` is the floor level (0 = ground level, 1-3 = upper floors)

## Expected Section

Specifies the expected results of the pathfinding.

```json
"expected": {
  "update": false,
  "pathFound": true,
  "pathLength": 4,
  "minimumPathLength": 2,
  "maximumPathLength": 10,
  "path": [
    [3222, 3218, 0],
    [3223, 3218, 0],
    [3224, 3218, 0],
    [3225, 3218, 0]
  ],
  "transportsUsed": ["FAIRY_RING", "GNOME_GLIDER"]
}
```

### Expectation Fields

| Field | Type | Description |
|-------|------|-------------|
| `update` | boolean | **Snapshot mode**: When `true`, runs pathfinding and updates this JSON file with actual results |
| `pathFound` | boolean | Whether a path should be found. Use `false` to test unreachable destinations |
| `pathLength` | integer | Exact expected path length (number of tiles) |
| `minimumPathLength` | integer | Minimum acceptable path length |
| `maximumPathLength` | integer | Maximum acceptable path length |
| `path` | array | **Required**: Exact expected path as list of coordinates |
| `transportsUsed` | array | Expected transport types used (not yet implemented) |

### Update Mode (Snapshot Generation)

When `"update": true` is set in the expected section, running the test will:

1. Execute the pathfinding with the given setup
2. Update the JSON file with actual results:
   - `update` is set to `false`
   - `pathFound` is set based on whether a path was found
   - `pathLength` is set to the actual path length
   - `path` is populated with the complete path coordinates
   - `minimumPathLength` and `maximumPathLength` are cleared
3. The test passes (snapshot update mode)

This is useful for:
- Generating initial expected values for a new test
- Updating expected values after intentional pathfinding changes
- Creating snapshot-like tests where you want to capture the exact current behavior

**Workflow for creating a new test:**
1. Create the JSON file with setup and test sections
2. Set `"update": true` in the expected section
3. Run the tests - the file will be updated with actual results
4. Review the generated path to ensure it's correct
5. Commit the updated JSON file

## Example Test Files

### Simple Walking Test

```json
{
  "name": "Simple walking path - Lumbridge to nearby",
  "description": "Basic test that verifies simple walking pathfinding works",
  "setup": {
    "pathfinder": {
      "avoidWilderness": true
    }
  },
  "test": {
    "start": [3222, 3218, 0],
    "end": [3225, 3218, 0]
  },
  "expected": {
    "update": true
  }
}
```

### Transport Test with Items

```json
{
  "name": "Fairy ring with Dramen staff",
  "description": "Tests fairy ring usage with required items",
  "setup": {
    "inventory": {
      "772": 1
    },
    "varbits": {
      "6571": 100
    },
    "pathfinder": {
      "useFairyRings": true
    }
  },
  "test": {
    "start": [3201, 3169, 0],
    "end": [2412, 4434, 0]
  },
  "expected": {
    "update": true
  }
}
```

## Running Tests

The JSON tests are run as part of the normal Gradle test task:

```bash
./gradlew test
```

Or run only the JSON-based tests:

```bash
./gradlew test --tests "shortestpath.pathfinder.json.PathfindingJsonTest"
```

Each JSON file will appear as a separate test in the test report, named after the `name` field in the JSON.
