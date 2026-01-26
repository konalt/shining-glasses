const { createCanvas, loadImage } = require("canvas");
const fs = require("fs");

const filename = process.argv[2];

const keyframeInterval = 9999;

let lastOutPixelsOn = [];
let lastOutPixelsOff = [];
let first = true;

async function run(i, kf = false) {
    console.log(`running frame ${i}`);

    const outPixelsOn = [];
    const outPixelsOff = [];

    const image = await loadImage("badapple/f" + i + ".png");
    const c = createCanvas(36, 12);
    const ctx = c.getContext("2d");
    ctx.drawImage(image, 36 / 2 - image.width / 2, 0, image.width, 12);

    const data = ctx.getImageData(0, 0, 36, 12);
    for (let i = 0; i < data.data.length; i += 4) {
        const x = (i / 4) % 36;
        const y = Math.floor(i / 4 / 36);
        if (data.data[i] > 128) {
            outPixelsOn.push([x, y]);
        } else {
            outPixelsOff.push([x, y]);
        }
    }

    let ifPixelsOn = [];
    let ifPixelsOff = [];
    if (kf) {
        ifPixelsOn = outPixelsOn;
        ifPixelsOff = outPixelsOff;
    } else {
        for (const px of outPixelsOn) {
            let find =
                first ||
                lastOutPixelsOff.find((p) => p[0] == px[0] && p[1] == px[1]);
            if (find) {
                ifPixelsOn.push(px);
            }
        }
        for (const px of outPixelsOff) {
            let find =
                first ||
                lastOutPixelsOn.find((p) => p[0] == px[0] && p[1] == px[1]);
            if (find) {
                ifPixelsOff.push(px);
            }
        }
    }

    lastOutPixelsOn = outPixelsOn;
    lastOutPixelsOff = outPixelsOff;
    first = false;

    const outText = `"${ifPixelsOn.map((p) => p.join(",")).join(" ")}|${ifPixelsOff.map((p) => p.join(",")).join(" ")}",`;

    fs.appendFileSync(`a_badapple.txt`, outText + "\n");
}

async function run2(c) {
    fs.writeFileSync("a_badapple.txt", "");
    for (let i = 1; i <= c; i++) {
        let kf = i % keyframeInterval == 0;
        await run(i.toString().padStart(4, "0"), kf);
    }
}

run2(874).then(() => {
    console.log("Done");
});
