use js_sys::Array;
use wasm_bindgen::prelude::*;
use web_sys::{CanvasRenderingContext2d, HtmlCanvasElement, HtmlImageElement};

// ─── Canvas dimensions ────────────────────────────────────────────────────────
const CANVAS_W: f64 = 1300.0;
const CANVAS_H: f64 = 720.0;

// ─── Game balance constants ────────────────────────────────────────────────────
const NUM_STARS: usize = 500;
const INITIAL_ENEMIES: usize = 5;
const MAX_ENEMIES: usize = 10;
const SCORE_PER_LEVEL: i32 = 250;
const MAX_SHIP_LEVEL: usize = 8;
const BULLET_DAMAGE_BASE: i32 = 75;
const ENEMY_BULLET_DAMAGE_MULTIPLIER: i32 = 5;

// ─── Image index layout (100 images, loaded by JS in this exact order) ─────────
//   0-2   stars (estrella1-3)
//   3-7   enemies (enemigo01-05)
//   8-10  player bullets (disparo1-3)
//   11    enemy bullet (disparo4)
//   12    life heart (vida)
//  13-20  player thruster frames (fuego1-8)
//  21-28  enemy thruster frames (fuegoenemigo1-8)
//  29-35  explosion frames (explosion-01 through -07)
//  36-99  ships: ship s (0-based), upgrade frame f (0-based) → 36 + s*8 + f
const IMG_STAR: usize = 0;
const IMG_ENEMY: usize = 3;
const IMG_BULLET_PLAYER: usize = 8;
const IMG_BULLET_ENEMY: usize = 11;
const IMG_LIFE: usize = 12;
const IMG_FIRE_PLAYER: usize = 13;
const IMG_FIRE_ENEMY: usize = 21;
const IMG_EXPLOSION: usize = 29;
const IMG_SHIP: usize = 36;

fn ship_img(ship: usize, frame: usize) -> usize {
    IMG_SHIP + ship * 8 + frame
}

// ─── Pseudo-random helpers (delegates to JS Math.random) ──────────────────────
fn rng() -> f64 {
    js_sys::Math::random()
}

fn rand_range(lo: f64, hi: f64) -> f64 {
    lo + rng() * (hi - lo)
}

// ─── Game entities ─────────────────────────────────────────────────────────────

struct Star {
    x: f64,
    y: f64,
    /// 1-3 (controls size/speed, matching original tipo field)
    kind: usize,
}

impl Star {
    fn random() -> Self {
        Star {
            x: rand_range(0.0, CANVAS_W),
            y: rand_range(0.0, CANVAS_H),
            kind: (rng() * 3.0) as usize + 1,
        }
    }

    fn spawn_at_top() -> Self {
        Star {
            x: rand_range(0.0, CANVAS_W),
            y: 0.0,
            kind: (rng() * 3.0) as usize + 1,
        }
    }
}

struct Ship {
    x: f64,
    y: f64,
    /// Current HP (0-100)
    health: i32,
    /// Remaining lives (starts at 3)
    lives: i32,
    /// Upgrade level 1-8 (determines sprite and bullet count)
    level: usize,
}

struct Enemy {
    x: f64,
    y: f64,
    health: i32,
    /// Enemy tier 1-5 (higher = faster but less HP)
    level: usize,
}

impl Enemy {
    fn random() -> Self {
        let level = (rng() * 5.0) as usize + 1;
        Enemy {
            x: rand_range(100.0, 1100.0),
            y: 0.0,
            health: (6 - level as i32) * 100,
            level,
        }
    }
}

struct Bullet {
    x: f64,
    y: f64,
    /// For enemy bullets this is the enemy's level, used for damage calculation
    kind: i32,
}

struct Explosion {
    x: f64,
    y: f64,
    /// Animation frame index 1-7
    progress: usize,
}

#[derive(PartialEq)]
enum Phase {
    Menu,
    Playing,
    GameOver,
}

// ─── Main game struct (exported to JavaScript) ─────────────────────────────────

#[wasm_bindgen]
pub struct SpaceWarGame {
    ctx: CanvasRenderingContext2d,
    images: Vec<HtmlImageElement>,

    phase: Phase,
    ship: Ship,
    /// 0-based index into ships 0..7 (player choice)
    chosen_ship: usize,

    enemies: Vec<Enemy>,
    bullets: Vec<Bullet>,
    enemy_bullets: Vec<Bullet>,
    stars: Vec<Star>,
    explosions: Vec<Explosion>,

    score: i32,
    /// Current animation frame for thruster fire (0-7, cycles at 50 ms)
    fire_frame: usize,
    last_ts: f64,

    // ── ms accumulators for sub-systems ──
    star_acc: f64,
    fire_acc: f64,
    spawn_acc: f64,
    spawn_interval: f64,
    shoot_acc: f64,
    shoot_interval: f64,

    // ── game-over ship explosion animation ──
    over_counter: u32,
    over_progress: usize,
}

#[wasm_bindgen]
impl SpaceWarGame {
    /// Create a new game from a canvas element and a flat JS Array of
    /// pre-loaded HTMLImageElement objects (see image layout above).
    #[wasm_bindgen(constructor)]
    pub fn new(canvas: HtmlCanvasElement, images_js: Array) -> SpaceWarGame {
        // Extract typed image elements from the JS Array
        let images: Vec<HtmlImageElement> = (0..images_js.length())
            .filter_map(|i| images_js.get(i).dyn_into::<HtmlImageElement>().ok())
            .collect();

        let ctx = canvas
            .get_context("2d")
            .unwrap_throw()
            .unwrap_throw()
            .dyn_into::<CanvasRenderingContext2d>()
            .unwrap_throw();

        // Initialise starfield
        let mut stars = Vec::with_capacity(NUM_STARS);
        for _ in 0..NUM_STARS {
            stars.push(Star::random());
        }

        SpaceWarGame {
            ctx,
            images,
            phase: Phase::Menu,
            ship: Ship { x: 620.0, y: 600.0, health: 100, lives: 3, level: 1 },
            chosen_ship: 0,
            enemies: Vec::new(),
            bullets: Vec::new(),
            enemy_bullets: Vec::new(),
            stars,
            explosions: Vec::new(),
            score: 0,
            fire_frame: 0,
            last_ts: 0.0,
            star_acc: 0.0,
            fire_acc: 0.0,
            spawn_acc: 0.0,
            spawn_interval: rand_range(500.0, 1500.0),
            shoot_acc: 0.0,
            shoot_interval: rand_range(250.0, 950.0),
            over_counter: 0,
            over_progress: 0,
        }
    }

    // ── Input handlers ────────────────────────────────────────────────────────

    /// Called on every keydown event.  `key` is KeyboardEvent.key.
    pub fn on_key_down(&mut self, key: String, key_code: u32) {
        match self.phase {
            Phase::Menu => {
                // Keys 1-8 (top row or numpad) choose a ship
                let choice: Option<usize> = match key_code {
                    49..=56 => Some((key_code - 48) as usize),  // top-row 1-8
                    97..=104 => Some((key_code - 96) as usize), // numpad 1-8
                    _ => None,
                };
                if let Some(c) = choice {
                    self.start_game(c);
                }
            }
            Phase::Playing => {
                if key == " " || key_code == 32 {
                    self.fire_player_bullets();
                }
            }
            Phase::GameOver => {
                if self.over_counter >= 25 {
                    self.phase = Phase::Menu;
                }
            }
        }
    }

    /// Called on every mousemove event with canvas-relative coordinates.
    pub fn on_mouse_move(&mut self, x: f64, y: f64) {
        if self.phase == Phase::Playing {
            // Centre the sprite on the cursor (ship sprite is 60×60)
            self.ship.x = x - 30.0;
            self.ship.y = y - 60.0;
        }
    }

    /// Main per-frame update + render, called from requestAnimationFrame.
    pub fn tick(&mut self, timestamp: f64) {
        // Compute delta-time in ms; cap to 100 ms to survive tab-switching
        let dt = if self.last_ts == 0.0 {
            16.667
        } else {
            (timestamp - self.last_ts).min(100.0)
        };
        self.last_ts = timestamp;

        self.update(dt);
        self.render();
    }
}

// ─── Private game logic ────────────────────────────────────────────────────────

impl SpaceWarGame {
    fn start_game(&mut self, ship_number: usize) {
        // ship_number is 1-based; clamp to valid range
        self.chosen_ship = ship_number.saturating_sub(1).min(7);

        self.ship = Ship { x: 620.0, y: 600.0, health: 100, lives: 3, level: 1 };
        self.enemies.clear();
        self.bullets.clear();
        self.enemy_bullets.clear();
        self.explosions.clear();
        self.score = 0;
        self.fire_frame = 0;
        self.last_ts = 0.0;
        self.star_acc = 0.0;
        self.fire_acc = 0.0;
        self.spawn_acc = 0.0;
        self.spawn_interval = rand_range(500.0, 1500.0);
        self.shoot_acc = 0.0;
        self.shoot_interval = rand_range(250.0, 950.0);
        self.over_counter = 0;
        self.over_progress = 0;

        for _ in 0..INITIAL_ENEMIES {
            self.enemies.push(Enemy::random());
        }

        self.phase = Phase::Playing;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    fn update(&mut self, dt: f64) {
        // Stars always scroll, regardless of game phase
        self.update_stars(dt);

        match self.phase {
            Phase::Playing => {
                self.update_ship_level();
                self.update_bullets(dt);
                self.update_enemies(dt);
                self.update_fire_and_explosions(dt);
                self.check_collisions();
                self.maybe_spawn_enemy(dt);
                self.maybe_enemy_shoot(dt);
            }
            Phase::GameOver => {
                self.update_fire_and_explosions(dt);
            }
            Phase::Menu => {}
        }
    }

    /// Raise the ship's upgrade level based on score thresholds.
    fn update_ship_level(&mut self) {
        let new_level = ((self.score / SCORE_PER_LEVEL) as usize + 1).min(MAX_SHIP_LEVEL);
        self.ship.level = new_level;
    }

    /// Scroll the starfield downward.  Original: y += kind*2 every 30 ms.
    fn update_stars(&mut self, dt: f64) {
        self.star_acc += dt;
        if self.star_acc < 30.0 {
            return;
        }
        let ticks = self.star_acc / 30.0;
        self.star_acc %= 30.0;

        for star in &mut self.stars {
            star.y += star.kind as f64 * 2.0 * ticks;
            if star.y > CANVAS_H {
                *star = Star::spawn_at_top();
            }
        }
    }

    /// Move bullets.  Original: player bullets y-=10, enemy bullets y+=10, every 15 ms.
    fn update_bullets(&mut self, dt: f64) {
        let ticks = dt / 15.0;
        let mut i = self.bullets.len();
        while i > 0 {
            i -= 1;
            self.bullets[i].y -= 10.0 * ticks;
            if self.bullets[i].y < 0.0 {
                self.bullets.swap_remove(i);
            }
        }

        let mut i = self.enemy_bullets.len();
        while i > 0 {
            i -= 1;
            self.enemy_bullets[i].y += 10.0 * ticks;
            if self.enemy_bullets[i].y > CANVAS_H {
                self.enemy_bullets.swap_remove(i);
            }
        }
    }

    /// Drop enemies downward.  Original: y += level every 15 ms.
    fn update_enemies(&mut self, dt: f64) {
        let ticks = dt / 15.0;
        for enemy in &mut self.enemies {
            enemy.y += enemy.level as f64 * ticks;
            if enemy.y > CANVAS_H {
                // Recycle off-screen enemies immediately
                *enemy = Enemy::random();
            }
        }
    }

    /// Advance the shared thruster fire animation (every 50 ms) and explosion
    /// progress (each explosion frame also advances every 50 ms).
    fn update_fire_and_explosions(&mut self, dt: f64) {
        self.fire_acc += dt;
        if self.fire_acc < 50.0 {
            return;
        }
        self.fire_acc %= 50.0;

        self.fire_frame = (self.fire_frame + 1) % 8;

        // Advance explosion animation; remove finished ones
        let mut i = self.explosions.len();
        while i > 0 {
            i -= 1;
            self.explosions[i].progress += 1;
            if self.explosions[i].progress > 7 {
                self.explosions.swap_remove(i);
            }
        }

        // Game-over ship explosion counter
        if self.phase == Phase::GameOver && self.over_counter < 25 {
            self.over_progress += 1;
            if self.over_progress >= 7 {
                self.over_progress = 0;
            }
            self.over_counter += 1;
        }
    }

    fn maybe_spawn_enemy(&mut self, dt: f64) {
        self.spawn_acc += dt;
        if self.spawn_acc >= self.spawn_interval {
            self.spawn_acc -= self.spawn_interval;
            self.spawn_interval = rand_range(500.0, 1500.0);
            if self.enemies.len() < MAX_ENEMIES {
                self.enemies.push(Enemy::random());
            }
        }
    }

    fn maybe_enemy_shoot(&mut self, dt: f64) {
        self.shoot_acc += dt;
        if self.shoot_acc < self.shoot_interval {
            return;
        }
        self.shoot_acc -= self.shoot_interval;
        let player_level = (self.score / SCORE_PER_LEVEL).min(8);
        // Shooting becomes more frequent as the player progresses
        let max_interval = 100.0 * (9 - player_level) as f64;
        self.shoot_interval = rand_range(250.0, max_interval + 250.0);

        // Every visible enemy fires a bullet
        let shots: Vec<Bullet> = self
            .enemies
            .iter()
            .map(|e| Bullet { x: e.x + 17.0, y: e.y + 40.0, kind: e.level as i32 })
            .collect();
        self.enemy_bullets.extend(shots);
    }

    /// AABB collision detection matching the original Java logic.
    fn check_collisions(&mut self) {
        let sx = self.ship.x;
        let sy = self.ship.y;
        let ship_level = self.ship.level as i32;

        // ── Player bullets vs enemies ──────────────────────────────────────────
        let mut j = self.bullets.len();
        'bullet_loop: while j > 0 {
            j -= 1;
            let bx = self.bullets[j].x;
            let by = self.bullets[j].y;

            let mut i = self.enemies.len();
            while i > 0 {
                i -= 1;
                let ex = self.enemies[i].x;
                let ey = self.enemies[i].y;

                let hit_x = (bx > ex && bx < ex + 60.0)
                    || (bx + 26.0 > ex && bx + 26.0 < ex + 60.0);
                let hit_y = by - 12.0 < ey + 30.0;

                if hit_x && hit_y {
                    self.bullets.swap_remove(j);
                    self.enemies[i].health -= ship_level * BULLET_DAMAGE_BASE;

                    if self.enemies[i].health <= 0 {
                        let (ex, ey, elv) =
                            (self.enemies[i].x, self.enemies[i].y, self.enemies[i].level);
                        self.score += elv as i32 * 10;
                        self.explosions
                            .push(Explosion { x: ex - 16.0, y: ey - 14.0, progress: 1 });
                        self.enemies.swap_remove(i);

                        if self.enemies.len() < 5 {
                            self.enemies.push(Enemy::random());
                        }
                    }
                    continue 'bullet_loop;
                }
            }
        }

        // ── Enemy bullets vs player ────────────────────────────────────────────
        let mut i = self.enemy_bullets.len();
        while i > 0 {
            i -= 1;
            let bx = self.enemy_bullets[i].x;
            let by = self.enemy_bullets[i].y;
            let bk = self.enemy_bullets[i].kind;

            let hit_x = (bx > sx && bx < sx + 60.0)
                || (bx + 26.0 > sx && bx + 26.0 < sx + 60.0);
            let hit_y = by - 12.0 > sy + 30.0;

            if hit_x && hit_y {
                self.enemy_bullets.swap_remove(i);
                self.ship.health -= bk * ENEMY_BULLET_DAMAGE_MULTIPLIER;

                if self.ship.health <= 0 {
                    self.ship.health = 100;
                    self.ship.lives -= 1;
                    if self.ship.lives <= 0 {
                        self.phase = Phase::GameOver;
                    }
                }
                break; // one hit per frame is enough
            }
        }
    }

    fn fire_player_bullets(&mut self) {
        let num = if self.ship.level == MAX_SHIP_LEVEL {
            3
        } else if self.ship.level >= 4 {
            2
        } else {
            1
        };

        match num {
            2 => {
                for i in 0..2usize {
                    self.bullets.push(Bullet {
                        x: self.ship.x + 15.0 + 30.0 * i as f64 - 13.0,
                        y: self.ship.y - 40.0,
                        kind: 0,
                    });
                }
            }
            3 => {
                for i in 0..3usize {
                    self.bullets.push(Bullet {
                        x: self.ship.x + 30.0 * i as f64 - 13.0,
                        y: self.ship.y - 40.0,
                        kind: 0,
                    });
                }
            }
            _ => {
                self.bullets.push(Bullet {
                    x: self.ship.x + 17.0,
                    y: self.ship.y - 40.0,
                    kind: 0,
                });
            }
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    fn render(&self) {
        let ctx = &self.ctx;

        // Background
        ctx.set_fill_style_str("black");
        ctx.fill_rect(0.0, 0.0, CANVAS_W, CANVAS_H);

        // Stars are always visible
        for star in &self.stars {
            self.draw_image(ctx, IMG_STAR + star.kind - 1, star.x, star.y);
        }

        match self.phase {
            Phase::Menu => self.render_menu(ctx),
            Phase::Playing => self.render_game(ctx),
            Phase::GameOver => self.render_game_over(ctx),
        }
    }

    fn render_menu(&self, ctx: &CanvasRenderingContext2d) {
        ctx.set_fill_style_str("white");
        ctx.set_font("bold 24px Arial, sans-serif");
        let _ = ctx.fill_text("Choose your ship  (press 1 – 8)", 430.0, 75.0);

        ctx.set_font("italic 14px Arial, sans-serif");

        let mut col: usize = 0;
        let mut row: usize = 0;
        for i in 0..8usize {
            let x = 150.0 + col as f64 * 300.0;
            let y = 180.0 + row as f64 * 300.0;
            let _ = ctx.fill_text(&format!("Ship {}  [{}]", i + 1, i + 1), x + 15.0, y);
            // Use the first upgrade frame (index 0) as the menu preview
            self.draw_image_scaled(ctx, ship_img(i, 0), x, y + 20.0, 80.0, 80.0);
            col += 1;
            if i == 3 {
                row = 1;
                col = 0;
            }
        }
    }

    fn render_game(&self, ctx: &CanvasRenderingContext2d) {
        // Player bullets — bullet sprite is selected by bullet count tier
        let bullet_sprite = IMG_BULLET_PLAYER
            + if self.ship.level == MAX_SHIP_LEVEL {
                2
            } else if self.ship.level >= 4 {
                1
            } else {
                0
            };
        for b in &self.bullets {
            self.draw_image(ctx, bullet_sprite, b.x, b.y);
        }

        // Enemy bullets
        for b in &self.enemy_bullets {
            self.draw_image(ctx, IMG_BULLET_ENEMY, b.x, b.y);
        }

        // Enemies + their thruster fire
        for e in &self.enemies {
            self.draw_image_scaled(ctx, IMG_ENEMY + e.level - 1, e.x, e.y, 60.0, 60.0);
            self.draw_image(ctx, IMG_FIRE_ENEMY + self.fire_frame, e.x + 15.0, e.y - 30.0);
        }

        // Explosion effects
        for exp in &self.explosions {
            if exp.progress >= 1 && exp.progress <= 7 {
                self.draw_image(ctx, IMG_EXPLOSION + exp.progress - 1, exp.x, exp.y);
            }
        }

        // Player ship — sprite is driven by upgrade level (1-8)
        self.draw_image_scaled(
            ctx,
            ship_img(self.chosen_ship, self.ship.level - 1),
            self.ship.x,
            self.ship.y,
            60.0,
            60.0,
        );
        // Player thruster fire
        self.draw_image(
            ctx,
            IMG_FIRE_PLAYER + self.fire_frame,
            self.ship.x + 15.0,
            self.ship.y + 50.0,
        );

        self.render_hud(ctx);
    }

    fn render_hud(&self, ctx: &CanvasRenderingContext2d) {
        ctx.set_stroke_style_str("lime");
        ctx.set_fill_style_str("lime");
        ctx.set_font("italic 14px Arial, sans-serif");

        // Health bar outline + fill
        ctx.stroke_rect(1215.0, 75.0, 25.0, 500.0);
        let health_h = self.ship.health as f64 * 5.0;
        ctx.fill_rect(1215.0, 500.0 + 75.0 - health_h, 25.0, health_h);

        // Text labels
        let _ = ctx.fill_text(&format!("HP: {}", self.ship.health), 1200.0, 65.0);
        let _ = ctx.fill_text(&format!("Level: {}", self.ship.level), 1205.0, 600.0);
        let _ = ctx.fill_text(&format!("Lives: {}", self.ship.lives), 1200.0, 625.0);
        let _ = ctx.fill_text(&format!("Score: {}", self.score), 1200.0, 650.0);

        // Life icons
        for i in 0..self.ship.lives as usize {
            self.draw_image_scaled(ctx, IMG_LIFE, 1185.0 + 30.0 * i as f64, 20.0, 25.0, 25.0);
        }
    }

    fn render_game_over(&self, ctx: &CanvasRenderingContext2d) {
        ctx.set_fill_style_str("white");
        ctx.set_font("bold 70px Arial, sans-serif");
        let _ = ctx.fill_text("Game Over", 425.0, 300.0);
        let _ = ctx.fill_text("Score: ", 425.0, 400.0);
        let _ = ctx.fill_text(&self.score.to_string(), 675.0, 400.0);

        if self.over_counter < 25 && self.over_progress < 7 {
            self.draw_image(
                ctx,
                IMG_EXPLOSION + self.over_progress,
                self.ship.x,
                self.ship.y,
            );
        }

        if self.over_counter >= 25 {
            ctx.set_font("italic 20px Arial, sans-serif");
            let _ = ctx.fill_text("Press any key to return to menu", 480.0, 500.0);
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    fn draw_image(&self, ctx: &CanvasRenderingContext2d, idx: usize, x: f64, y: f64) {
        if let Some(img) = self.images.get(idx) {
            let _ = ctx.draw_image_with_html_image_element(img, x, y);
        }
    }

    fn draw_image_scaled(
        &self,
        ctx: &CanvasRenderingContext2d,
        idx: usize,
        x: f64,
        y: f64,
        w: f64,
        h: f64,
    ) {
        if let Some(img) = self.images.get(idx) {
            let _ = ctx.draw_image_with_html_image_element_and_dw_and_dh(img, x, y, w, h);
        }
    }
}
